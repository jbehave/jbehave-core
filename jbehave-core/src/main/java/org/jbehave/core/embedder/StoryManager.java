package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder.RunningStoriesFailed;
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.ReportsCount;
import org.jbehave.core.steps.InjectableStepsFactory;

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

    public List<RunningStory> runningStories(List<String> storyPaths, MetaFilter filter, BatchFailures failures,
            State beforeStories) {
        List<RunningStory> runningStories = new ArrayList<RunningStory>();
        for (String storyPath : storyPaths) {
            Story story = storyOfPath(storyPath);
            runningStories.add(runningStory(storyPath, story, filter, failures, beforeStories));
        }
        return runningStories;
    }

    public RunningStory runningStory(String storyPath, Story story, MetaFilter filter, BatchFailures batchFailures,
            State beforeStories) {
        EnqueuedStory enqueuedStory = new EnqueuedStory(storyPath, story, configuration, stepsFactory, filter,
                embedderControls, batchFailures, embedderMonitor, storyRunner, beforeStories);
        return submit(enqueuedStory);
    }

    public void waitUntilAllDoneOrFailed(List<RunningStory> runningStories, BatchFailures failures) {
        long start = System.currentTimeMillis();
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (RunningStory runningStory : runningStories) {
                Future<ThrowableStory> future = runningStory.getFuture();
                if (!future.isDone()) {
                    allDone = false;
                    long durationInSecs = storyDurationInSecs(start);
                    long timeoutInSecs = embedderControls.storyTimeoutInSecs();
                    if (durationInSecs > timeoutInSecs) {
                        Story story = runningStory.getStory();
                        StoryDuration storyDuration = new StoryDuration(durationInSecs, timeoutInSecs);
                        embedderMonitor.storyTimeout(story, storyDuration);
                        storyRunner.cancelStory(story, storyDuration);
                        future.cancel(true);
                    }
                    break;
                } else {
                    Story story = runningStory.getStory();
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
        // cancel any outstanding execution which is not done before returning
        for (RunningStory runningStory : runningStories) {
            Future<ThrowableStory> future = runningStory.getFuture();
            if (!future.isDone()) {
                future.cancel(true);
            }
        }

    }

    private void tickTock() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }

    private long storyDurationInSecs(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }

    private synchronized RunningStory submit(EnqueuedStory enqueuedStory) {
        return new RunningStory(enqueuedStory.getStory(), executorService.submit(enqueuedStory));
    }

    private static class EnqueuedStory implements Callable<ThrowableStory> {
        private final String storyPath;
        private final Configuration configuration;
        private final InjectableStepsFactory stepsFactory;
        private final Story story;
        private final MetaFilter filter;
        private final EmbedderControls embedderControls;
        private final BatchFailures batchFailures;
        private final EmbedderMonitor embedderMonitor;
        private final StoryRunner storyRunner;
        private final State beforeStories;

        public EnqueuedStory(String storyPath, Story story, Configuration configuration,
                InjectableStepsFactory stepsFactory, MetaFilter filter, EmbedderControls embedderControls,
                BatchFailures batchFailures, EmbedderMonitor embedderMonitor, StoryRunner storyRunner,
                State beforeStories) {
            this.storyPath = storyPath;
            this.configuration = configuration;
            this.stepsFactory = stepsFactory;
            this.story = story;
            this.filter = filter;
            this.embedderControls = embedderControls;
            this.batchFailures = batchFailures;
            this.embedderMonitor = embedderMonitor;
            this.storyRunner = storyRunner;
            this.beforeStories = beforeStories;
        }

        public ThrowableStory call() throws Exception {
            try {
                embedderMonitor.runningStory(storyPath);
                storyRunner.run(configuration, stepsFactory, story, filter, beforeStories);
            } catch (Throwable e) {
                if (embedderControls.batch()) {
                    // collect and postpone decision to throw exception
                    batchFailures.put(storyPath, e);
                } else {
                    if (embedderControls.ignoreFailureInStories()) {
                        embedderMonitor.storyFailed(storyPath, e);
                    } else {
                        return new ThrowableStory(story, new StoryExecutionFailed(storyPath, e));
                    }
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
    }

}
