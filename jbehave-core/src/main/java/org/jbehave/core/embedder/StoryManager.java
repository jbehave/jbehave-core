package org.jbehave.core.embedder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
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
import org.jbehave.core.embedder.StoryTimeouts.TimeoutParser;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * Manages the execution and outcomes of running stories. While each story is
 * run by the {@link PerformableTree}, the manager is responsible for the concurrent
 * submission and monitoring of their execution via the {@link ExecutorService}.
 */
public class StoryManager {

	private final Configuration configuration;
	private final EmbedderControls embedderControls;
	private final EmbedderMonitor embedderMonitor;
	private final ExecutorService executorService;
	private final InjectableStepsFactory stepsFactory;
	private final PerformableTree performableTree;
	private final Map<String, RunningStory> runningStories = new HashMap<>();
	private final Map<MetaFilter, List<Story>> excludedStories = new HashMap<>();
	private RunContext context;
	private StoryTimeouts timeouts;
	
	public StoryManager(Configuration configuration,
			InjectableStepsFactory stepsFactory,
			EmbedderControls embedderControls, EmbedderMonitor embedderMonitor,
			ExecutorService executorService, PerformableTree performableTree, TimeoutParser... parsers) {
		this.configuration = configuration;
		this.embedderControls = embedderControls;
		this.embedderMonitor = embedderMonitor;
		this.executorService = executorService;
		this.stepsFactory = stepsFactory;
		this.performableTree = performableTree;
		this.timeouts = new StoryTimeouts(embedderControls, embedderMonitor);
		this.timeouts.withParsers(parsers);
	}

	public Story storyOfPath(String storyPath) {
		return performableTree.storyOfPath(configuration, storyPath);
	}

	public List<Story> storiesOfPaths(List<String> storyPaths) {
		List<Story> stories = new ArrayList<>(storyPaths.size());
		for (String storyPath : storyPaths) {
			stories.add(storyOfPath(storyPath));
		}
		return configuration.isParallelStoryExamplesEnabled() ? StorySplitter.splitStories(stories) : stories;
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
		List<StoryOutcome> outcomes = new ArrayList<>();
		for (RunningStory story : runningStories.values()) {
			outcomes.add(new StoryOutcome(story));
		}
		return outcomes;
	}

	public void runStoriesAsPaths(List<String> storyPaths, MetaFilter filter,
			BatchFailures failures) {
		runStories(storiesOfPaths(storyPaths), filter, failures);
	}

	public void runStories(List<Story> stories, MetaFilter filter,
			BatchFailures failures) {
		// create new run context
		context = performableTree.newRunContext(configuration, stepsFactory.createCandidateSteps(),
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
		runStories(context, stories);
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

    private void runStories(RunContext context, List<Story> stories) {
        stories.stream()
               .sorted(configuration.storyExecutionComparator())
               .forEach(story -> filterRunning(context, story));
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
			stories = new ArrayList<>();
			excludedStories.put(filter, stories);
		}
		return stories;
	}

	public RunningStory runningStory(Story story) {
		return submit(new EnqueuedStory(performableTree, context,
				embedderControls, embedderMonitor, story, timeouts));
	}

    public void waitUntilAllDoneOrFailed(RunContext context) {
        if ( runningStories.values().isEmpty() ) {
        	return;
        }
        boolean allDone = false;
        boolean started = false;
        while (!allDone || !started) {
            allDone = true;
            for (RunningStory runningStory : runningStories.values()) {
                if ( runningStory.isStarted() ){
                	started = true;
                    Story story = runningStory.getStory();
					Future<ThrowableStory> future = runningStory.getFuture();
					if (!future.isDone()) {
						allDone = false;
						StoryDuration duration = runningStory.getDuration();
						runningStory.updateDuration();
						if (context.isCancelled(story)) {
							if (duration.cancelTimedOut()) {
								future.cancel(true);
							}
							continue;
						}
						if (duration.timedOut()) {
							embedderMonitor.storyTimeout(story, duration);
							context.cancelStory(story, duration);
							if (embedderControls.failOnStoryTimeout()) {
								throw new StoryExecutionFailed(story.getPath(),
										new StoryTimedOut(duration));
							}
							continue;
						}
					} else {
						try {
							ThrowableStory throwableStory = future.get();
							Throwable throwable = throwableStory.getThrowable();
							if (throwable != null) {
								context.addFailure(story, throwable);
								if (!embedderControls.ignoreFailureInStories()) {
									continue;
								}
							}
						} catch (Throwable e) {
							context.addFailure(story, e);
							if (!embedderControls.ignoreFailureInStories()) {
								continue;
							}
						}
					}
                } else {
                	started = false;
                	allDone = false;
                }
            }
            tickTock();
        }
        writeStoryDurations(runningStories.values());
	}

	protected void writeStoryDurations(Collection<RunningStory> runningStories) {
		// collect story durations and cancel any outstanding execution which is
		// not done before returning
		Properties storyDurations = new Properties();
		long total = 0;
		for (RunningStory runningStory : runningStories) {
			long durationInMillis = runningStory.getDurationInMillis();
			total += durationInMillis;
			storyDurations.setProperty(runningStory.getStory().getPath(),
					Long.toString(durationInMillis));
			Future<ThrowableStory> future = runningStory.getFuture();
			if (!future.isDone()) {
				future.cancel(true);
			}
		}
		int threads = embedderControls.threads();
		long threadAverage = total / threads;
		storyDurations.setProperty("total", Long.toString(total));
		storyDurations.setProperty("threads", Long.toString(threads));
		storyDurations.setProperty("threadAverage",
				Long.toString(threadAverage));
		write(storyDurations, "storyDurations.props");
	}

	private void write(Properties p, String name) {
		File outputDirectory = configuration.storyReporterBuilder()
				.outputDirectory();
		try {
			outputDirectory.mkdirs();
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

	static class EnqueuedStory implements Callable<ThrowableStory> {

		private final PerformableTree performableTree;
		private final RunContext context;
		private final EmbedderControls embedderControls;
		private final EmbedderMonitor embedderMonitor;
		private final Story story;
		private final StoryTimeouts timeouts;
		private long startedAtMillis;

		public EnqueuedStory(PerformableTree performableTree,
				RunContext context, EmbedderControls embedderControls,
				EmbedderMonitor embedderMonitor, Story story, StoryTimeouts timeouts) {
			this.performableTree = performableTree;
			this.context = context;
			this.embedderControls = embedderControls;
			this.embedderMonitor = embedderMonitor;
			this.story = story;
			this.timeouts = timeouts;
		}

		@Override
        public ThrowableStory call() {
		    startedAtMillis = System.currentTimeMillis();
			String storyPath = story.getPath();
			try {
				embedderMonitor.runningStory(storyPath);
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
			return timeouts.getTimeoutInSecs(story);
		}

	}

	@SuppressWarnings("serial")
	public static class StoryExecutionFailed extends RuntimeException {

		public StoryExecutionFailed(String storyPath, Throwable failure) {
			super(storyPath, failure);
		}

	}

    @SuppressWarnings("serial")
    public static class StoryTimedOut extends RuntimeException {

		public StoryTimedOut(StoryDuration storyDuration) {
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
