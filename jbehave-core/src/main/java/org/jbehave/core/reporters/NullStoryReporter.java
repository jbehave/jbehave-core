package org.jbehave.core.reporters;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.Timing;

/**
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null-object</a> implementation of
 * {@link StoryReporter}. Users can subclass it and can override only the method that they
 * are interested in.
 * </p>
 */
public class NullStoryReporter implements StoryReporter {

    @Override
    public void beforeStoriesSteps(Stage stage) {
        // Do nothing by default
    }

    @Override
    public void afterStoriesSteps(Stage stage) {
        // Do nothing by default
    }

    @Override
    public void beforeStep(Step step) {
        // Do nothing by default
    }

    @Override
    public void successful(String step) {
        // Do nothing by default
    }

    @Override
    public void ignorable(String step) {
        // Do nothing by default
    }

    @Override
    public void comment(String step) {
        // Do nothing by default
    }

    @Override
    public void pending(PendingStep step) {
        // Do nothing by default
    }

    @Override
    @Deprecated
    public void pending(String step) {
        // Do nothing by default
    }

    @Override
    public void notPerformed(String step) {
        // Do nothing by default
    }

    @Override
    public void failed(String step, Throwable cause) {
        // Do nothing by default
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        // Do nothing by default
    }

    @Override
    public void storyExcluded(Story story, String filter) {
        // Do nothing by default
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        // Do nothing by default
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        // Do nothing by default
    }

    @Override
    public void afterStory(boolean givenStory) {
        // Do nothing by default
    }

    @Override
    public void beforeScenarios() {
        // Do nothing by default
    }

    @Override
    public void afterScenarios() {
        // Do nothing by default
    }

    @Override
    public void narrative(final Narrative narrative) {
        // Do nothing by default
    }

    @Override
    public void lifecycle(Lifecycle lifecycle) {
        // Do nothing by default
    }

    @Override
    public void beforeStorySteps(Stage stage, Lifecycle.ExecutionType type) {
        // Do nothing by default
    }

    @Override
    public void afterStorySteps(Stage stage, Lifecycle.ExecutionType type) {
        // Do nothing by default
    }

    @Override
    public void beforeScenarioSteps(Stage stage, Lifecycle.ExecutionType type) {
        // Do nothing by default
    }

    @Override
    public void afterScenarioSteps(Stage stage, Lifecycle.ExecutionType type) {
        // Do nothing by default
    }

    @Override
    public void beforeComposedSteps() {
        // Do nothing by default
    }

    @Override
    public void afterComposedSteps() {
        // Do nothing by default
    }

    @Override
    public void beforeGivenStories() {
        // Do nothing by default
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        // Do nothing by default
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        // Do nothing by default
    }

    @Override
    public void afterGivenStories() {
        // Do nothing by default
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        // Do nothing by default
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter) {
        // Do nothing by default
    }

    @Override
    public void afterScenario(Timing timing) {
        // Do nothing by default
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        // Do nothing by default
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        // Do nothing by default
    }

    @Override
    public void afterExamples() {
        // Do nothing by default
    }

    @Override
    public void dryRun() {
        // Do nothing by default
    }

    @Override
    public void pendingMethods(List<String> methods) {
        // Do nothing by default
    }

    @Override
    public void restarted(String step, Throwable cause) {
        // Do nothing by default
    }

    @Override
    public void restartedStory(Story story, Throwable cause) {
        // Do nothing by default
    }
}
