package org.jbehave.core.embedder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * Manages the execution and outcomes of running stories. While each story is
 * run by the {@link StoryRunner}, the manager is responsible for the concurrent
 * submission and monitoring of their execution via the {@link ExecutorService}.
 */
public class StoryManager {

	private final Configuration configuration;
	private final EmbedderControls embedderControls;
	private final EmbedderMonitor embedderMonitor;
	private final ExecutorService executorService;
	private final InjectableStepsFactory stepsFactory;
	private final PerformableTree performableTree;
	private final Map<String, RunningStory> runningStories = new HashMap<String, RunningStory>();
	private final Map<MetaFilter, List<Story>> excludedStories = new HashMap<MetaFilter, List<Story>>();
	private RunContext context;

	public StoryManager(Configuration configuration,
			InjectableStepsFactory stepsFactory,
			EmbedderControls embedderControls, EmbedderMonitor embedderMonitor,
			ExecutorService executorService, PerformableTree performableTree) {
		this.configuration = configuration;
		this.embedderControls = embedderControls;
		this.embedderMonitor = embedderMonitor;
		this.executorService = executorService;
		this.stepsFactory = stepsFactory;
		this.performableTree = performableTree;
	}

	public Story storyOfPath(String storyPath) {
		return performableTree.storyOfPath(configuration, storyPath);
	}

	public Story storyOfText(String storyAsText, String storyId) {
		return performableTree.storyOfText(configuration, storyAsText, storyId);
	}

	public void clear() {
		runningStories.clear();
	}

	public PerformableRoot performableRoot() {
		return performableTree.getRoot();
	}

	public List<StoryOutcome> outcomes() {
		List<StoryOutcome> outcomes = new ArrayList<StoryOutcome>();
		for (RunningStory story : runningStories.values()) {
			outcomes.add(new StoryOutcome(story));
		}
		return outcomes;
	}

	public void runStoriesAsPaths(List<String> storyPaths, MetaFilter filter,
			BatchFailures failures) {
		runStories(storiesOf(storyPaths), filter, failures);
	}

	private List<Story> storiesOf(List<String> storyPaths) {
		List<Story> stories = new ArrayList<Story>();
		for (String storyPath : storyPaths) {
			stories.add(storyOfPath(storyPath));
		}
		return stories;
	}

	public void runStories(List<Story> stories, MetaFilter filter,
			BatchFailures failures) {
		// create new run context
		context = performableTree.newRunContext(configuration, stepsFactory,
				embedderMonitor, filter, failures);

		// add stories
		performableTree.addStories(context, stories);

		// perform stories
		performStories(context, performableTree, stories);

		// collect failures
		failures.putAll(context.getFailures());

	}

	private void performStories(RunContext context,
			PerformableTree performableTree, List<Story> stories) {
		// before stories
		performableTree.performBeforeOrAfterStories(context, Stage.BEFORE);

		// run stories
		runningStories(context, stories);
		waitUntilAllDoneOrFailed(context);
		MetaFilter filter = context.filter();
		List<Story> notAllowed = notAllowedBy(filter);
		if (!notAllowed.isEmpty()) {
			embedderMonitor.storiesNotAllowed(notAllowed, filter,
					embedderControls.verboseFiltering());
		}

		// after stories
		performableTree.performBeforeOrAfterStories(context, Stage.AFTER);
	}

	public Map<String, RunningStory> runningStories(RunContext context,
			List<Story> stories) {
		for (Story story : stories) {
			filterRunning(context, story);
		}
		return runningStories;
	}

	private void filterRunning(RunContext context, Story story) {
		FilteredStory filteredStory = context.filter(story);
		if (filteredStory.allowed()) {
			runningStories.put(story.getPath(), runningStory(story));
		} else {
			notAllowedBy(context.getFilter()).add(story);
		}
	}

	public List<Story> notAllowedBy(MetaFilter filter) {
		List<Story> stories = excludedStories.get(filter);
		if (stories == null) {
			stories = new ArrayList<Story>();
			excludedStories.put(filter, stories);
		}
		return stories;
	}

	public RunningStory runningStory(Story story) {
		return submit(new EnqueuedStory(performableTree, context,
				embedderControls, embedderMonitor, story));
	}

    public void waitUntilAllDoneOrFailed(RunContext context) {
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (RunningStory runningStory : runningStories.values()) {            	
                if ( runningStory.isStarted() ){
                    Story story = runningStory.getStory();
					Future<ThrowableStory> future = runningStory.getFuture();
					if (!future.isDone()) {
						allDone = false;
						StoryDuration duration = runningStory.getDuration();
						runningStory.updateDuration();
						if (duration.timedOut()) {
							embedderMonitor.storyTimeout(story, duration);
							context.cancelStory(story, duration);
							future.cancel(true);
							if (embedderControls.failOnStoryTimeout()) {
								throw new StoryExecutionFailed(story.getPath(),
										new StoryTimeout(duration));
							}
							continue;
						}
					} else {
						try {
							ThrowableStory throwableStory = future.get();
							Throwable throwable = throwableStory.getThrowable();
							if (throwable != null) {
								context.addFailure(story.getPath(), throwable);
								if (!embedderControls.ignoreFailureInStories()) {
									continue;
								}
							}
						} catch (Throwable e) {
							context.addFailure(story.getPath(), e);
							if (!embedderControls.ignoreFailureInStories()) {
								continue;
							}
						}
					}
                }
            }
            tickTock();
        }
        // collect story durations and cancel any outstanding execution which is not done before returning
        Properties storyDurations = new Properties();
        long total = 0;
        for (RunningStory runningStory : runningStories.values()) {
        	long durationInMillis = runningStory.getDurationInMillis();
        	total += durationInMillis;
			storyDurations.setProperty(runningStory.getStory().getPath(), Long.toString(durationInMillis));
            Future<ThrowableStory> future = runningStory.getFuture();
            if (!future.isDone()) {
                future.cancel(true);
            }            
        }
        storyDurations.setProperty("total", Long.toString(total));
        write(storyDurations, "storyDurations.props");
    }

	private void write(Properties p, String name) {
		File outputDirectory = configuration.storyReporterBuilder()
				.outputDirectory();
		try {
			Writer output = new FileWriter(new File(outputDirectory, name));
			p.store(output, this.getClass().getName());
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void tickTock() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}

	private synchronized RunningStory submit(EnqueuedStory enqueuedStory) {
		return new RunningStory(enqueuedStory, executorService.submit(enqueuedStory));
	}

	private static class EnqueuedStory implements Callable<ThrowableStory> {

		private final PerformableTree performableTree;
		private final RunContext context;
		private final EmbedderControls embedderControls;
		private final EmbedderMonitor embedderMonitor;
		private final Story story;
		private long startedAtMillis;

		public EnqueuedStory(PerformableTree performableTree,
				RunContext context, EmbedderControls embedderControls,
				EmbedderMonitor embedderMonitor, Story story) {
			this.performableTree = performableTree;
			this.context = context;
			this.embedderControls = embedderControls;
			this.embedderMonitor = embedderMonitor;
			this.story = story;
		}

		public ThrowableStory call() throws Exception {
			String storyPath = story.getPath();
			try {
				embedderMonitor.runningStory(storyPath);
				startedAtMillis = System.currentTimeMillis();
				performableTree.perform(context, story);
			} catch (Throwable e) {
				if (embedderControls.ignoreFailureInStories()) {
					embedderMonitor.storyFailed(storyPath, e);
				} else {
					return new ThrowableStory(story, new StoryExecutionFailed(
							storyPath, e));
				}
			}
			return new ThrowableStory(story, null);
		}

		public Story getStory() {
			return story;
		}

		public long getStartedAtMillis() {
			return startedAtMillis;
		}

		public long getTimeoutInSecs() {
			return embedderControls.storyTimeoutInSecs();
		}

	}

	@SuppressWarnings("serial")
	public static class StoryExecutionFailed extends RuntimeException {

		public StoryExecutionFailed(String storyPath, Throwable failure) {
			super(storyPath, failure);
		}

	}

    @SuppressWarnings("serial")
    public static class StoryTimeout extends RuntimeException {

		public StoryTimeout(StoryDuration storyDuration) {
			super(storyDuration.getDurationInSecs() + "s > "
					+ storyDuration.getTimeoutInSecs() + "s");
		}

	}

	public static class ThrowableStory {
		private Story story;
		private Throwable throwable;

		public ThrowableStory(Story story, Throwable throwable) {
			this.story = story;
			this.throwable = throwable;
		}

		public Story getStory() {
			return story;
		}

		public Throwable getThrowable() {
			return throwable;
		}
	}

	public static class RunningStory {
		private EnqueuedStory enqueuedStory;
		private Future<ThrowableStory> future;
		private StoryDuration duration;

		public RunningStory(EnqueuedStory enqueuedStory,
				Future<ThrowableStory> future) {
			this.enqueuedStory = enqueuedStory;
			this.future = future;
		}

		public Future<ThrowableStory> getFuture() {
			return future;
		}

		public Story getStory() {
			return enqueuedStory.getStory();
		}

		public long getDurationInMillis() {
			if ( duration == null ){
				return 0;
			}
			return duration.getDurationInSecs() * 1000;
		}

		public StoryDuration getDuration() {
			if (duration == null) {
				duration = new StoryDuration(enqueuedStory.getStartedAtMillis(), enqueuedStory.getTimeoutInSecs());
			}
			return duration;
		}

		public void updateDuration() {
			duration.update();
		}

		public boolean isDone() {
			return future.isDone();
		}

		public boolean isFailed() {
			if (isDone()) {
				try {
					return future.get().getThrowable() != null;
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
			return false;
		}
		
		public boolean isStarted(){
			return enqueuedStory.getStartedAtMillis() != 0;
		}
	}

	public static class StoryOutcome {
		private String path;
		private Boolean done;
		private Boolean failed;

		public StoryOutcome(RunningStory story) {
			this.path = story.getStory().getPath();
			this.done = story.isDone();
			this.failed = story.isFailed();
		}

		public String getPath() {
			return path;
		}

		public Boolean isDone() {
			return done;
		}

		public Boolean isFailed() {
			return failed;
		}

	}
	
}
