package org.jbehave.core.reporters;

import org.jbehave.core.model.*;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Timing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * When running a multithreading mode, reports cannot be written concurrently but should 
 * be delayed and invoked only at the end of a story, ensuring synchronization on the delegate
 * responsible for the reporting.
 */
public class ConcurrentStoryReporter implements StoryReporter {

    private static Method storyCancelled;
    private static Method storyNotAllowed;
    private static Method beforeStory;
    private static Method afterStory;
    private static Method narrative;
    private static Method lifecycle;
    private static Method beforeStorySteps;
    private static Method afterStorySteps;
    private static Method beforeScenarioSteps;
    private static Method afterScenarioSteps;
    private static Method scenarioNotAllowed;
    private static Method beforeScenarios;
    private static Method beforeScenario;
    private static Method afterScenarioDeprecated;
    private static Method afterScenario;
    private static Method afterScenarios;
    private static Method beforeGivenStories;
    private static Method givenStories;
    private static Method givenStoriesPaths;
    private static Method afterGivenStories;
    private static Method beforeExamples;
    private static Method example;
    private static Method afterExamples;
    private static Method beforeStep;
    private static Method successful;
    private static Method ignorable;
    private static Method comment;
    private static Method pending;
    private static Method notPerformed;
    private static Method failed;
    private static Method failedOutcomes;
    private static Method dryRun;
    private static Method pendingMethods;
    private static Method restarted;
    private static Method restartedStory;

    static {
        try {
            storyCancelled = StoryReporter.class.getMethod("storyCancelled", Story.class, StoryDuration.class);
            storyNotAllowed = StoryReporter.class.getMethod("storyNotAllowed", Story.class, String.class);
            beforeStory = StoryReporter.class.getMethod("beforeStory", Story.class, Boolean.TYPE);
            afterStory = StoryReporter.class.getMethod("afterStory", Boolean.TYPE);
            narrative = StoryReporter.class.getMethod("narrative", Narrative.class);
            lifecycle = StoryReporter.class.getMethod("lifecyle", Lifecycle.class);
            beforeStorySteps = StoryReporter.class.getMethod("beforeStorySteps", Stage.class);
            afterStorySteps = StoryReporter.class.getMethod("afterStorySteps", Stage.class);
            beforeScenarioSteps = StoryReporter.class.getMethod("beforeScenarioSteps", Stage.class);
            afterScenarioSteps = StoryReporter.class.getMethod("afterScenarioSteps", Stage.class);
            scenarioNotAllowed = StoryReporter.class.getMethod("scenarioNotAllowed", Scenario.class, String.class);
            beforeScenarios = StoryReporter.class.getMethod("beforeScenarios");
            beforeScenario = StoryReporter.class.getMethod("beforeScenario", Scenario.class);
            afterScenarioDeprecated = StoryReporter.class.getMethod("afterScenario");
            afterScenario = StoryReporter.class.getMethod("afterScenario", Timing.class);
            afterScenarios = StoryReporter.class.getMethod("afterScenarios");
            beforeGivenStories = StoryReporter.class.getMethod("beforeGivenStories");
            givenStories = StoryReporter.class.getMethod("givenStories", GivenStories.class);
            givenStoriesPaths = StoryReporter.class.getMethod("givenStories", List.class);
            afterGivenStories = StoryReporter.class.getMethod("afterGivenStories");
            beforeExamples = StoryReporter.class.getMethod("beforeExamples", List.class, ExamplesTable.class);
            example = StoryReporter.class.getMethod("example", Map.class, int.class);
            afterExamples = StoryReporter.class.getMethod("afterExamples");
            beforeStep = StoryReporter.class.getMethod("beforeStep", String.class);
            successful = StoryReporter.class.getMethod("successful", String.class);
            ignorable = StoryReporter.class.getMethod("ignorable", String.class);
            comment = StoryReporter.class.getMethod("comment", String.class);
            pending = StoryReporter.class.getMethod("pending", String.class);
            notPerformed = StoryReporter.class.getMethod("notPerformed", String.class);
            failed = StoryReporter.class.getMethod("failed", String.class, Throwable.class);
            failedOutcomes = StoryReporter.class.getMethod("failedOutcomes", String.class, OutcomesTable.class);
            dryRun = StoryReporter.class.getMethod("dryRun");
            pendingMethods = StoryReporter.class.getMethod("pendingMethods", List.class);
            restarted = StoryReporter.class.getMethod("restarted", String.class, Throwable.class);
            restartedStory = StoryReporter.class.getMethod("restartedStory", Story.class, Throwable.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final StoryReporter crossReferencing;
    private final StoryReporter delegate;
    private final boolean multiThreading;
    private final List<DelayedMethod> delayedMethods;
    private boolean invoked = false;

    public ConcurrentStoryReporter(StoryReporter crossReferencing, StoryReporter delegate, boolean multiThreading) {
        this.crossReferencing = crossReferencing;
        this.delegate = delegate;
        this.multiThreading = multiThreading;
        this.delayedMethods = multiThreading ? Collections.synchronizedList(new ArrayList<DelayedMethod>()) : null;
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        perform(reporter -> reporter.storyNotAllowed(story, filter), storyNotAllowed, story, filter);
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        perform(reporter ->  reporter.beforeStory(story, givenStory), beforeStory, story, givenStory);
    }

    @Override
    public void afterStory(boolean givenStory) {
        perform(reporter ->  reporter.afterStory(givenStory), afterStory, givenStory);
    }

    @Override
    public void narrative(Narrative aNarrative) {
        perform(reporter ->  reporter.narrative(aNarrative), narrative, aNarrative);
    }
    
    @Override
    public void lifecyle(Lifecycle aLifecycle) {
        perform(reporter ->  reporter.lifecyle(aLifecycle), lifecycle, aLifecycle);
    }

    @Override
    public void beforeStorySteps(Stage stage) {
        perform(reporter ->  reporter.beforeStorySteps(stage), beforeStorySteps, stage);
    }

    @Override
    public void afterStorySteps(Stage stage) {
        perform(reporter ->  reporter.afterStorySteps(stage), afterStorySteps, stage);
    }

    @Override
    public void beforeScenarioSteps(Stage stage) {
        perform(reporter ->  reporter.beforeScenarioSteps(stage), beforeScenarioSteps, stage);
    }

    @Override
    public void afterScenarioSteps(Stage stage) {
        perform(reporter ->  reporter.afterScenarioSteps(stage), afterScenarioSteps, stage);
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        perform(reporter ->  reporter.scenarioNotAllowed(scenario, filter), scenarioNotAllowed, scenario, filter);
    }

    @Override
    public void beforeScenarios() {
        perform(StoryReporter::beforeScenarios, beforeScenarios);
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        perform(reporter ->  reporter.beforeScenario(scenario), beforeScenario, scenario);
    }

    @Override
    public void afterScenario() {
        perform(StoryReporter::afterScenario, afterScenarioDeprecated);
    }

    @Override
    public void afterScenario(Timing timing) {
        perform(reporter -> reporter.afterScenario(timing), afterScenario, timing);
    }

    @Override
    public void afterScenarios() {
        perform(StoryReporter::afterScenarios, afterScenarios);
    }

    @Override
    public void beforeGivenStories() {
        perform(StoryReporter::beforeGivenStories, beforeGivenStories);
    }

    @Override
    public void givenStories(GivenStories stories) {
        perform(reporter ->  reporter.givenStories(stories), givenStories, stories);
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        perform(reporter ->  reporter.givenStories(storyPaths), givenStoriesPaths, storyPaths);
    }

    @Override
    public void afterGivenStories() {
        perform(StoryReporter::afterGivenStories, afterGivenStories);
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        perform(reporter ->  reporter.beforeExamples(steps, table), beforeExamples, steps, table);
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        perform(reporter ->  reporter.example(tableRow, exampleIndex), example, tableRow, exampleIndex);
    }

    @Override
    public void afterExamples() {
        perform(StoryReporter::afterExamples, afterExamples);
    }

    @Override
    public void beforeStep(String step) {
        perform(reporter ->  reporter.beforeStep(step), beforeStep, step);
    }

    @Override
    public void successful(String step) {
        perform(reporter ->  reporter.successful(step), successful, step);
    }

    @Override
    public void ignorable(String step) {
        perform(reporter ->  reporter.ignorable(step), ignorable, step);
    }

    @Override
    public void comment(String step) {
        perform(reporter ->  reporter.comment(step), comment, step);
    }

    @Override
    public void pending(String step) {
        perform(reporter ->  reporter.pending(step), pending, step);
    }

    @Override
    public void notPerformed(String step) {
        perform(reporter ->  reporter.notPerformed(step), notPerformed, step);
    }

    @Override
    public void failed(String step, Throwable cause) {
        perform(reporter ->  reporter.failed(step, cause), failed, step, cause);
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        perform(reporter ->  reporter.failedOutcomes(step, table), failedOutcomes, step, table);
    }

    @Override
    public void dryRun() {
        perform(StoryReporter::dryRun, dryRun);
    }

    @Override
    public void pendingMethods(List<String> methods) {
        perform(reporter ->  reporter.pendingMethods(methods), pendingMethods, methods);
    }
    
    @Override
    public void restarted(String step, Throwable cause) {
        perform(reporter ->  reporter.restarted(step, cause), restarted, step, cause);
    }
    
    @Override
    public void restartedStory(Story story, Throwable cause){
        perform(reporter ->  reporter.restartedStory(story, cause), restartedStory, story, cause);
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        perform(reporter ->  reporter.storyCancelled(story, storyDuration), storyCancelled, story, storyDuration);
    }

    private void perform(Consumer<StoryReporter> crossReferencingInvoker, Method delayedMethod, Object... delayedMethodArgs) {
        crossReferencingInvoker.accept(crossReferencing);
        if (multiThreading) {
            delayedMethods.add(new DelayedMethod(delayedMethod, delayedMethodArgs));
        } else {
            crossReferencingInvoker.accept(delegate);
        }
    }

    public StoryReporter getDelegate() {
        return delegate;
    }

    public boolean invoked(){
    	return invoked;
    }
    
    public void invokeDelayed() {
        if ( !multiThreading ){
            return;
        }
        synchronized (delegate) {
            for (DelayedMethod delayed : delayedMethods) {
                delayed.invoke(delegate);
            }
            delayedMethods.clear();
        }
        invoked = true;
    }

    public static class DelayedMethod {
        private Method method;
        private Object[] args;

        public DelayedMethod(Method method, Object... args) {
            this.method = method;
            this.args = args;
        }

        public void invoke(StoryReporter delegate) {
            try {
                method.invoke(delegate, args);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("" + method, e);
            }
        }
    }
}
