package org.jbehave.core.reporters;

import org.jbehave.core.model.*;
import org.jbehave.core.steps.Timing;
import org.jbehave.core.steps.StepCollector.Stage;

import java.util.List;
import java.util.Map;

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
    }

    @Override
    public void afterStoriesSteps(Stage stage) {
    }

    @Override
    public void beforeStep(String step) {
    }

    @Override
    public void successful(String step) {
    }

    @Override
    public void ignorable(String step) {
    }

    @Override
    public void comment(String step) {
    }

    @Override
    public void pending(String step) {
    }

    @Override
    public void notPerformed(String step) {
    }

    @Override
    public void failed(String step, Throwable cause) {
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
    }

    @Override
    public void storyExcluded(Story story, String filter) {
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
    }

    @Override
    public void afterStory(boolean givenStory) {
    }

    @Override
    public void beforeScenarios() {
    }

    @Override
    public void afterScenarios() {
    }

    @Override
    public void narrative(final Narrative narrative) {
    }

    @Override
    public void lifecycle(Lifecycle lifecycle) {
    }

    @Override
    public void beforeStorySteps(Stage stage) {
    }

    @Override
    public void afterStorySteps(Stage stage) {
    }

    @Override
    public void beforeScenarioSteps(Stage stage) {
    }

    @Override
    public void afterScenarioSteps(Stage stage) {
    }

    @Override
    public void beforeComposedSteps() {
    }

    @Override
    public void afterComposedSteps() {
    }

    @Override
    public void beforeGivenStories(){
    }

    @Override
    public void givenStories(GivenStories givenStories) {
    }

    @Override
    public void givenStories(List<String> storyPaths) {
    }

    @Override
    public void afterGivenStories(){
    }

    @Override
    public void beforeScenario(Scenario scenario) {
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter) {
    }

    @Override
    public void afterScenario(Timing timing) {
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
    }

    @Override
    public void afterExamples() {
    }

    @Override
    public void dryRun() {
    }

    @Override
    public void pendingMethods(List<String> methods) {
    }

    @Override
    public void restarted(String step, Throwable cause) {
    }

    @Override
    public void restartedStory(Story story, Throwable cause) {
    }
}
