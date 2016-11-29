package org.jbehave.core.reporters;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.failures.StepFailed;
import org.jbehave.core.model.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * When a step fails, the {@link Throwable} that caused the failure is wrapped
 * in a {@link StepFailed} together with the step during which the failure
 * occurred. If such a failure occurs it will throw the {@link StepFailed}
 * after the story is finished.
 * </p>
 * 
 * @see StepFailed
 */
public class StepFailureDecorator implements StoryReporter {

    private final StoryReporter delegate;
    private UUIDExceptionWrapper failure;

    public StepFailureDecorator(StoryReporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void afterScenario() {
        delegate.afterScenario();
    }

    @Override
    public void afterStory(boolean givenStory) {
        delegate.afterStory(givenStory);
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        delegate.beforeScenario(scenarioTitle);
    }

    @Override
    public void scenarioMeta(Meta meta) {
        delegate.scenarioMeta(meta);
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        failure = null;
        delegate.beforeStory(story, givenStory);
    }

    @Override
    public void narrative(Narrative narrative) {
        delegate.narrative(narrative);
    }

    @Override
    public void lifecyle(Lifecycle lifecycle) {
        delegate.lifecyle(lifecycle);
    }

    @Override
    public void failed(String step, Throwable cause) {
        failure = (UUIDExceptionWrapper) cause;
        delegate.failed(step, failure);
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        failure = new StepFailed(step, table);
        delegate.failedOutcomes(step, table);
    }
    
    @Override
    public void beforeStep(String step) {
        delegate.beforeStep(step);
    }

    @Override
    public void ignorable(String step) {
        delegate.ignorable(step);
    }

    @Override
    public void comment(String step) {
        delegate.comment(step);
    }

    @Override
    public void notPerformed(String step) {
        delegate.notPerformed(step);
    }

    @Override
    public void pending(String step) {
        delegate.pending(step);
    }

    @Override
    public void successful(String step) {
        delegate.successful(step);
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        delegate.givenStories(givenStories);
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        delegate.givenStories(storyPaths);
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        delegate.beforeExamples(steps, table);
    }

    @Override
    public void example(Map<String, String> tableRow) {
        delegate.example(tableRow);
    }

    @Override
    public void afterExamples() {
        delegate.afterExamples();
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        delegate.scenarioNotAllowed(scenario, filter);
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        delegate.storyNotAllowed(story, filter);
    }

    @Override
    public void dryRun() {
        delegate.dryRun();
    }

    @Override
    public void pendingMethods(List<String> methods) {
        delegate.pendingMethods(methods);
    }

    @Override
    public void restarted(String step, Throwable cause) {
        delegate.restarted(step, cause);
    }
    
    @Override
    public void restartedStory(Story story, Throwable cause) {
        delegate.restartedStory(story, cause);
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        delegate.storyCancelled(story, storyDuration);
    }
}
