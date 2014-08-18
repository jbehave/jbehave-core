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
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.CandidateSteps;
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
    private final StoryRunner storyRunner;
    private final Map<String, RunningStory> runningStories = new HashMap<String, RunningStory>();
    private final Map<MetaFilter, List<Story>> excludedStories = new HashMap<MetaFilter, List<Story>>();
    
    public StoryManager(Configuration configuration, EmbedderControls embedderControls,
            EmbedderMonitor embedderMonitor, ExecutorService executorService, InjectableStepsFactory stepsFactory,
            StoryRunner storyRunner) {
        this.configuration = configuration;
        this.embedderControls = embedderControls;
        this.embedderMonitor = embedderMonitor;
        this.executorService = executorService;
        this.stepsFactory = stepsFactory;
        this.storyRunner = storyRunner;
    }

    public Story storyOfPath(String storyPath) {
        return storyRunner.storyOfPath(configuration, storyPath);
    }

    public Story storyOfText(String storyAsText, String storyId) {
        return storyRunner.storyOfText(configuration, storyAsText, storyId);
    }

    public void clear() {
        runningStories.clear();
    }

    public List<StoryOutcome> outcomes() {
        List<StoryOutcome> outcomes = new ArrayList<StoryOutcome>();
        for (RunningStory story : runningStories.values()) {
            outcomes.add(new StoryOutcome(story));
        }
        return outcomes;
    }
    
    public void runStories(List<String> storyPaths, MetaFilter filter, BatchFailures failures) {
        // configure cross reference with meta filter
        if ( configuration.storyReporterBuilder().hasCrossReference() ){
            configuration.storyReporterBuilder().crossReference().withMetaFilter(filter.asString());
        }
        
        // run before stories
        State beforeStories = runBeforeOrAfterStories(failures, Stage.BEFORE);

        // run stories as paths
        runningStoriesAsPaths(storyPaths, filter, beforeStories);
        waitUntilAllDoneOrFailed(failures);
        List<Story> notAllowed = notAllowedBy(filter);
        if (!notAllowed.isEmpty()) {
            embedderMonitor.storiesNotAllowed(notAllowed, filter, embedderControls.verboseFiltering());
        }

        // run after stories
       runBeforeOrAfterStories(failures, Stage.AFTER);
    }

    public State runBeforeOrAfterStories(BatchFailures failures, Stage stage) {
        List<CandidateSteps> candidateSteps = stepsFactory.createCandidateSteps();
        State state = storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, stage);
        if (storyRunner.failed(state)) {
            failures.put(state.toString(), storyRunner.failure(state));
        }
        return state;
    }

    public Map<String, RunningStory> runningStoriesAsPaths(List<String> storyPaths, MetaFilter filter,
            State beforeStories) {
        for (String storyPath : storyPaths) {
            filterRunning(filter, beforeStories, storyPath, storyOfPath(storyPath));
        }
        return runningStories;
    }

    public Map<String, RunningStory> runningStories(List<Story> stories, MetaFilter filter, State beforeStories) {
        for (Story story : stories) {
            filterRunning(filter, beforeStories, story.getPath(), story);
        }
        return runningStories;
    }

    private void filterRunning(MetaFilter filter, State beforeStories, String storyPath, Story story) {
        FilteredStory filteredStory = new FilteredStory(filter, story, configuration.storyControls());
        if (filteredStory.allowed()) {
            runningStories.put(storyPath, runningStory(storyPath, story, filter, beforeStories));
        } else {
            notAllowedBy(filter).add(story);
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

    public RunningStory runningStory(String storyPath, Story story, MetaFilter filter, State beforeStories) {
        return submit(new EnqueuedStory(storyRunner, configuration, stepsFactory, embedderControls, embedderMonitor,
                storyPath, story, filter, beforeStories));
    }

    public void waitUntilAllDoneOrFailed(BatchFailures failures) {
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (RunningStory runningStory : runningStories.values()) {
                Future<ThrowableStory> future = runningStory.getFuture();
                Story story = runningStory.getStory();
                long storyTimeoutInSecs = embedderControls.storyTimeoutInSecs();
                StoryDuration storyDuration = runningStory.getDuration(storyTimeoutInSecs);
				if (!future.isDone()) {
                    allDone = false;
                    runningStory.updateDuration();
                    if ( storyDuration.timedOut() ) {
                        embedderMonitor.storyTimeout(story, storyDuration);
                        storyRunner.cancelStory(story, storyDuration);
                        future.cancel(true);
						if (embedderControls.failOnStoryTimeout()) {
							throw new StoryExecutionFailed(story.getPath(),
									new StoryTimeout(storyDuration));
						}
                    }
                    break;
                } else {
                    try {
                        ThrowableStory throwableStory = future.get();
                        Throwable throwable = throwableStory.getThrowable();
                        if (throwable != null) {
                            failures.put(story.getPath(), throwable);
                            if (!embedderControls.ignoreFailureInStories()) {
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        failures.put(story.getPath(), e);
                        if (!embedderControls.ignoreFailureInStories()) {
                            break;
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
        File outputDirectory = configuration.storyReporterBuilder().outputDirectory();
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
        return new RunningStory(enqueuedStory.getStory(), executorService.submit(enqueuedStory));
    }

    private static class EnqueuedStory implements Callable<ThrowableStory> {
        private final StoryRunner storyRunner;
        private final Configuration configuration;
        private final InjectableStepsFactory stepsFactory;
        private final EmbedderControls embedderControls;
        private final EmbedderMonitor embedderMonitor;
        private final String storyPath;
        private final Story story;
        private final MetaFilter filter;
        private final State beforeStories;

        private EnqueuedStory(StoryRunner storyRunner, Configuration configuration,
                InjectableStepsFactory stepsFactory, EmbedderControls embedderControls,
                EmbedderMonitor embedderMonitor, String storyPath, Story story, MetaFilter filter, State beforeStories) {
            this.storyRunner = storyRunner;
            this.configuration = configuration;
            this.stepsFactory = stepsFactory;
            this.embedderControls = embedderControls;
            this.embedderMonitor = embedderMonitor;
            this.storyPath = storyPath;
            this.story = story;
            this.filter = filter;
            this.beforeStories = beforeStories;
        }

        public ThrowableStory call() throws Exception {
            try {
                embedderMonitor.runningStory(storyPath);
                storyRunner.run(configuration, stepsFactory, story, filter, beforeStories);
            } catch (Throwable e) {
                if (embedderControls.ignoreFailureInStories()) {
                    embedderMonitor.storyFailed(storyPath, e);
                } else {
                    return new ThrowableStory(story, new StoryExecutionFailed(storyPath, e));
                }
            }
            return new ThrowableStory(story, null);
        }

        public Story getStory() {
            return story;
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
			super(storyDuration.getDurationInSecs() + "s > " + storyDuration.getTimeoutInSecs() + "s");
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
        private Story story;
        private StoryDuration duration;
        private Future<ThrowableStory> future;
        
        public RunningStory(Story story, Future<ThrowableStory> future) {
            this.story = story;
            this.future = future;
        }

		public Future<ThrowableStory> getFuture() {
            return future;
        }

        public Story getStory() {
            return story;
        }

        public long getDurationInMillis() {
        	if ( duration == null ){
        		return 0;
        	}
			return duration.getDurationInSecs() * 1000;
		}

        public StoryDuration getDuration(long timeoutInSecs) {
            if ( duration == null ){
            	duration = new StoryDuration(timeoutInSecs);
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
