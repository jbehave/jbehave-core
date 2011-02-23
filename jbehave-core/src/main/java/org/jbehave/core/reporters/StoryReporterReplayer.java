package org.jbehave.core.reporters;

import org.jbehave.core.model.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reports cannot handle being written to concurrently by a multi-threaded JBehave.
 * Only at the end of a story, should the reports phase be invoked.
 */
public class StoryReporterReplayer implements StoryReporter {

    private static Method storyNotAllowedMethod;
    private static Method beforeStoryMethod;
    private static Method narrativeMethod;
    private static Method afterStoryMethod;
    private static Method scenarioNotAllowedMethod;
    private static Method beforeScenarioMethod;
    private static Method scenarioMetaMethod;
    private static Method afterScenarioMethod;
    private static Method givenStories1Method;
    private static Method givenStories2Method;
    private static Method beforeExamplesMethod;
    private static Method exampleMethod;
    private static Method afterExamplesMethod;
    private static Method successfulMethod;
    private static Method ignorableMethod;
    private static Method pendingMethod;
    private static Method notPerformedMethod;
    private static Method failedMethod;
    private static Method failedOutcomesMethod;
    private static Method dryRunMethod;

    static {
        try {
            storyNotAllowedMethod = StoryReporter.class.getMethod("storyNotAllowed", Story.class, String.class);
            beforeStoryMethod = StoryReporter.class.getMethod("beforeStory", Story.class, Boolean.TYPE);
            narrativeMethod = StoryReporter.class.getMethod("narrative", Narrative.class);
            afterStoryMethod = StoryReporter.class.getMethod("afterStory", Boolean.TYPE);
            scenarioNotAllowedMethod = StoryReporter.class.getMethod("scenarioNotAllowed", Scenario.class, String.class);
            beforeScenarioMethod = StoryReporter.class.getMethod("beforeScenario", String.class);
            scenarioMetaMethod = StoryReporter.class.getMethod("scenarioMeta", Meta.class);
            afterScenarioMethod = StoryReporter.class.getMethod("afterScenario");
            givenStories1Method = StoryReporter.class.getMethod("givenStories", GivenStories.class);
            givenStories2Method = StoryReporter.class.getMethod("givenStories", List.class);
            beforeExamplesMethod = StoryReporter.class.getMethod("beforeExamples", List.class, ExamplesTable.class);
            exampleMethod = StoryReporter.class.getMethod("example", Map.class);
            afterExamplesMethod = StoryReporter.class.getMethod("afterExamples");
            successfulMethod = StoryReporter.class.getMethod("successful", String.class);
            ignorableMethod = StoryReporter.class.getMethod("ignorable", String.class);
            pendingMethod = StoryReporter.class.getMethod("pending", String.class);
            notPerformedMethod = StoryReporter.class.getMethod("notPerformed", String.class);
            failedMethod = StoryReporter.class.getMethod("failed", String.class, Throwable.class);
            failedOutcomesMethod = StoryReporter.class.getMethod("failedOutcomes", String.class, OutcomesTable.class);
            dryRunMethod = StoryReporter.class.getMethod("dryRun");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<MethodAndArgs> toReplay = new ArrayList<MethodAndArgs>();
    protected StoryReporter crossReference;
    protected StoryReporter delegate;
    private boolean replayed = false;

    public StoryReporterReplayer(StoryReporter crossReference, StoryReporter delegate) {
        this.crossReference = crossReference;
        if (this.crossReference == null) {
            this.crossReference = new NullStoryReporter();
        }
        this.delegate = delegate;
    }

    public static class MethodAndArgs {
        Method method;
        Object[] args;

        public MethodAndArgs(Method method, Object... args) {
            this.method = method;
            this.args = args;
        }
    }

    public void storyNotAllowed(Story story, String filter) {
        crossReference.storyNotAllowed(story, filter);
        toReplay.add(new MethodAndArgs(storyNotAllowedMethod, story, filter));
    }

    public void beforeStory(Story story, boolean givenStory) {
        crossReference.beforeStory(story, givenStory);
        toReplay.add(new MethodAndArgs(beforeStoryMethod, story, givenStory));
    }

    public void narrative(Narrative narrative) {
        crossReference.narrative(narrative);
        toReplay.add(new MethodAndArgs(narrativeMethod, narrative));
    }

    public void afterStory(boolean givenStory) {
        crossReference.afterStory(givenStory);
        toReplay.add(new MethodAndArgs(afterStoryMethod, givenStory));
    }

    public void scenarioNotAllowed(Scenario scenario, String filter) {
        crossReference.scenarioNotAllowed(scenario, filter);
        toReplay.add(new MethodAndArgs(scenarioNotAllowedMethod, scenario, filter));
    }

    public void beforeScenario(String scenarioTitle) {
        crossReference.beforeScenario(scenarioTitle);
        toReplay.add(new MethodAndArgs(beforeScenarioMethod, scenarioTitle));
    }

    public void scenarioMeta(Meta meta) {
        crossReference.scenarioMeta(meta);
        toReplay.add(new MethodAndArgs(scenarioMetaMethod, meta));
    }

    public void afterScenario() {
        crossReference.afterScenario();
        toReplay.add(new MethodAndArgs(afterScenarioMethod));
    }

    public void givenStories(GivenStories givenStories) {
        crossReference.givenStories(givenStories);
        toReplay.add(new MethodAndArgs(givenStories1Method, givenStories));
    }

    public void givenStories(List<String> storyPaths) {
        crossReference.givenStories(storyPaths);
        toReplay.add(new MethodAndArgs(givenStories2Method, storyPaths));
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
        crossReference.beforeExamples(steps, table);
        toReplay.add(new MethodAndArgs(beforeExamplesMethod, steps, table));
    }

    public void example(Map<String, String> tableRow) {
        crossReference.example(tableRow);
        toReplay.add(new MethodAndArgs(exampleMethod, tableRow));
    }

    public void afterExamples() {
        crossReference.afterExamples();
        toReplay.add(new MethodAndArgs(afterExamplesMethod));
    }

    public void successful(String step) {
        crossReference.successful(step);
        toReplay.add(new MethodAndArgs(successfulMethod, step));
    }

    public void ignorable(String step) {
        crossReference.ignorable(step);
        toReplay.add(new MethodAndArgs(ignorableMethod, step));
    }

    public void pending(String step) {
        crossReference.pending(step);
        toReplay.add(new MethodAndArgs(pendingMethod, step));
    }

    public void notPerformed(String step) {
        crossReference.notPerformed(step);
        toReplay.add(new MethodAndArgs(notPerformedMethod, step));
    }

    public void failed(String step, Throwable cause) {
        crossReference.failed(step, cause);
        toReplay.add(new MethodAndArgs(failedMethod, step, cause));
    }

    public void failedOutcomes(String step, OutcomesTable table) {
        crossReference.failedOutcomes(step, table);
        toReplay.add(new MethodAndArgs(failedOutcomesMethod, step, table));
    }

    public void dryRun() {
        crossReference.dryRun();
        toReplay.add(new MethodAndArgs(dryRunMethod));
    }

    public void replay() {
        if (replayed) {
            throw new RuntimeException("already replayed");
        }
        synchronized (delegate) {
            for (MethodAndArgs methodAndArgs : toReplay) {
                try {
                    methodAndArgs.method.invoke(delegate, methodAndArgs.args);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(""+ methodAndArgs.method,e);
                }
            }
        }
        replayed = true;
    }

}
