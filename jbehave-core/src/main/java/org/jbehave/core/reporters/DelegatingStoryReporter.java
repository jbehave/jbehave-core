package org.jbehave.core.reporters;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
import org.jbehave.core.steps.LifecycleStepsType;
import org.jbehave.core.steps.Timing;

/**
 * Reporter which collects other {@link StoryReporter}s and delegates all
 * invocations to the collected reporters.
 * 
 * @author Mirko FriedenHagen
 */
public class DelegatingStoryReporter implements StoryReporter {

    private final Collection<StoryReporter> delegates;

    /**
     * Creates DelegatingStoryReporter with a given collections of delegates
     * 
     * @param delegates the ScenarioReporters to delegate to
     */
    public DelegatingStoryReporter(Collection<StoryReporter> delegates) {
        this.delegates = delegates;
    }

    /**
     * Creates DelegatingStoryReporter with a given varargs of delegates
     * 
     * @param delegates the StoryReporters to delegate to
     */
    public DelegatingStoryReporter(StoryReporter... delegates) {
        this(asList(delegates));
    }

    @Override
    public void beforeStoriesSteps(Stage stage) {
        delegate(reporter -> reporter.beforeStoriesSteps(stage));
    }

    @Override
    public void afterStoriesSteps(Stage stage) {
        delegate(reporter -> reporter.afterStoriesSteps(stage));
    }

    @Override
    public void afterScenario(Timing timing) {
        delegate(reporter -> reporter.afterScenario(timing));
    }

    @Override
    public void afterScenarios() {
        delegate(StoryReporter::afterScenarios);
    }

    @Override
    public void afterStory(boolean givenStory) {
        delegate(reporter -> reporter.afterStory(givenStory));
    }

    @Override
    public void beforeScenarios() {
        delegate(StoryReporter::beforeScenarios);
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        delegate(reporter -> reporter.beforeScenario(scenario));
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        delegate(reporter -> reporter.beforeStory(story, givenStory));
    }

    @Override
    public void narrative(final Narrative narrative) {
        delegate(reporter -> reporter.narrative(narrative));
    }

    @Override
    public void lifecycle(Lifecycle lifecycle) {
        delegate(reporter -> reporter.lifecycle(lifecycle));
    }

    @Override
    public void beforeScenarioSteps(Stage stage, LifecycleStepsType type) {
        delegate(reporter -> reporter.beforeScenarioSteps(stage, type));
    }

    @Override
    public void afterScenarioSteps(Stage stage, LifecycleStepsType type) {
        delegate(reporter -> reporter.afterScenarioSteps(stage, type));
    }

    @Override
    public void beforeComposedSteps() {
        delegate(StoryReporter::beforeComposedSteps);
    }

    @Override
    public void afterComposedSteps() {
        delegate(StoryReporter::afterComposedSteps);
    }

    @Override
    public void beforeStorySteps(Stage stage, LifecycleStepsType type) {
        delegate(reporter -> reporter.beforeStorySteps(stage, type));
    }

    @Override
    public void afterStorySteps(Stage stage, LifecycleStepsType type) {
        delegate(reporter -> reporter.afterStorySteps(stage, type));
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        delegate(reporter -> reporter.beforeExamples(steps, table));
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        delegate(reporter -> reporter.example(tableRow, exampleIndex));
    }

    @Override
    public void afterExamples() {
        delegate(StoryReporter::afterExamples);
    }

    @Override
    public void failed(String step, Throwable cause) {
        delegate(reporter -> reporter.failed(step, cause));
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        delegate(reporter -> reporter.failedOutcomes(step, table));
    }

    @Override
    public void beforeGivenStories() {
        delegate(StoryReporter::beforeGivenStories);
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        delegate(reporter -> reporter.givenStories(givenStories));
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        delegate(reporter -> reporter.givenStories(storyPaths));
    }

    @Override
    public void afterGivenStories() {
        delegate(StoryReporter::afterGivenStories);
    }

    @Override
    public void beforeStep(Step step) {
        delegate(reporter -> reporter.beforeStep(step));
    }

    @Override
    public void ignorable(String step) {
        delegate(reporter -> reporter.ignorable(step));
    }

    @Override
    public void comment(String step) {
        delegate(reporter -> reporter.comment(step));
    }

    @Override
    public void notPerformed(String step) {
        delegate(reporter -> reporter.notPerformed(step));
    }

    @Override
    public void pending(String step) {
        delegate(reporter -> reporter.pending(step));
    }

    @Override
    public void successful(String step) {
        delegate(reporter -> reporter.successful(step));
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter) {
        delegate(reporter -> reporter.scenarioExcluded(scenario, filter));
    }

    @Override
    public void storyExcluded(Story story, String filter) {
        delegate(reporter -> reporter.storyExcluded(story, filter));
    }

    @Override
    public void dryRun() {
        delegate(StoryReporter::dryRun);
    }
    
    @Override
    public void pendingMethods(List<String> methods) {
        delegate(reporter -> reporter.pendingMethods(methods));
    }

    @Override
    public void restarted(String step, Throwable cause) {
        delegate(reporter -> reporter.restarted(step, cause));
    }
    
    @Override
    public void restartedStory(Story story, Throwable cause) {
        delegate(reporter -> reporter.restartedStory(story, cause));
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        delegate(reporter -> reporter.storyCancelled(story, storyDuration));
    }

    private void delegate(Consumer<StoryReporter> reporter) {
        delegates.forEach(reporter);
    }

    public Collection<StoryReporter> getDelegates() {
        return delegates;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
