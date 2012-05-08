package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.jbehave.core.configuration.Configuration;
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

    public StoryManager(Configuration configuration, InjectableStepsFactory stepsFactory,
            EmbedderControls embedderControls, EmbedderMonitor embedderMonitor, ExecutorService executorService,
            PerformableTree performableTree) {
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

    public List<StoryOutcome> outcomes() {
        List<StoryOutcome> outcomes = new ArrayList<StoryOutcome>();
        for (RunningStory story : runningStories.values()) {
            outcomes.add(new StoryOutcome(story));
        }
        return outcomes;
    }

    public void runStories(List<String> storyPaths, MetaFilter filter, BatchFailures failures) {
        // configure cross reference with meta filter
        if (configuration.storyReporterBuilder().hasCrossReference()) {
            configuration.storyReporterBuilder().crossReference().withMetaFilter(filter.asString());
        }

        // create new run context
        context = performableTree.newRunContext(configuration, stepsFactory, filter, failures);
        performableTree.addStories(context, storyPaths);
        
        // before stories
        performableTree.performBeforeOrAfterStories(context, Stage.BEFORE);
        
        // stories as paths
        runningStoriesAsPaths(context, storyPaths);        
        waitUntilAllDoneOrFailed(context);
        List<Story> notAllowed = notAllowedBy(filter);
        if (!notAllowed.isEmpty()) {
            embedderMonitor.storiesNotAllowed(notAllowed, filter, embedderControls.verboseFiltering());
        }

        // after stories
        performableTree.performBeforeOrAfterStories(context, Stage.AFTER);     
        
        // collect failures
        failures.putAll(context.getFailures());

    }

    public Map<String, RunningStory> runningStoriesAsPaths(RunContext context, List<String> storyPaths) {
        for (String storyPath : storyPaths) {
            filterRunning(context, storyOfPath(storyPath));
        }
        return runningStories;
    }

    public Map<String, RunningStory> runningStories(RunContext context, List<Story> stories) {
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
        return submit(new EnqueuedStory(performableTree, context, embedderControls, embedderMonitor, story));
    }

    public void waitUntilAllDoneOrFailed(RunContext context) {
        long start = System.currentTimeMillis();
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (RunningStory runningStory : runningStories.values()) {
                Future<ThrowableStory> future = runningStory.getFuture();
                if (!future.isDone()) {
                    allDone = false;
                    long durationInSecs = storyDurationInSecs(start);
                    long timeoutInSecs = embedderControls.storyTimeoutInSecs();
                    if (durationInSecs > timeoutInSecs) {
                        Story story = runningStory.getStory();
                        StoryDuration storyDuration = new StoryDuration(durationInSecs, timeoutInSecs);
                        embedderMonitor.storyTimeout(story, storyDuration);
                        context.cancelStory(story, storyDuration);
                        future.cancel(true);
                    }
                    break;
                } else {
                    Story story = runningStory.getStory();
                    try {
                        ThrowableStory throwableStory = future.get();
                        Throwable throwable = throwableStory.getThrowable();
                        if (throwable != null) {
                            context.addFailure(story.getPath(), throwable);
                            if (!embedderControls.ignoreFailureInStories()) {
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        context.addFailure(story.getPath(), e);
                        if (!embedderControls.ignoreFailureInStories()) {
                            break;
                        }
                    }
                }
            }
            tickTock();
        }
        // cancel any outstanding execution which is not done before returning
        for (RunningStory runningStory : runningStories.values()) {
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

        private final PerformableTree performableTree;
        private final RunContext context;
        private final EmbedderControls embedderControls;
        private final EmbedderMonitor embedderMonitor;
        private final Story story;

        public EnqueuedStory(PerformableTree performableTree, RunContext context, EmbedderControls embedderControls,
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
                performableTree.perform(context, story);
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
