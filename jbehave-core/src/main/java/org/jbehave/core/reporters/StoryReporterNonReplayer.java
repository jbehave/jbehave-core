package org.jbehave.core.reporters;

import org.jbehave.core.model.*;

import java.util.List;
import java.util.Map;

 public class StoryReporterNonReplayer extends StoryReporterReplayer {

    public StoryReporterNonReplayer(StoryReporter crossReference, StoryReporter delegate) {
        super(crossReference, delegate);
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        crossReference.storyNotAllowed(story, filter);
        delegate.storyNotAllowed(story, filter);
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        crossReference.beforeStory(story, givenStory);
        delegate.beforeStory(story, givenStory);
    }

    @Override
    public void narrative(Narrative narrative) {
        crossReference.narrative(narrative);
        delegate.narrative(narrative);
    }

    @Override
    public void afterStory(boolean givenStory) {
        crossReference.afterStory(givenStory);
        delegate.afterStory(givenStory);
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        crossReference.scenarioNotAllowed(scenario, filter);
        delegate.scenarioNotAllowed(scenario, filter);
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        crossReference.beforeScenario(scenarioTitle);
        delegate.beforeScenario(scenarioTitle);
    }

    @Override
    public void scenarioMeta(Meta meta) {
        crossReference.scenarioMeta(meta);
        delegate.scenarioMeta(meta);
    }

    @Override
    public void afterScenario() {
        crossReference.afterScenario();
        delegate.afterScenario();
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        crossReference.givenStories(givenStories);
        delegate.givenStories(givenStories);
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        crossReference.givenStories(storyPaths);
        delegate.givenStories(storyPaths);
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        crossReference.beforeExamples(steps, table);
        delegate.beforeExamples(steps, table);
    }

    @Override
    public void example(Map<String, String> tableRow) {
        crossReference.example(tableRow);
        delegate.example(tableRow);
    }

    @Override
    public void afterExamples() {
        crossReference.afterExamples();
        delegate.afterExamples();
    }

    @Override
    public void successful(String step) {
        crossReference.successful(step);
        delegate.successful(step);
    }

    @Override
    public void ignorable(String step) {
        crossReference.ignorable(step);
        delegate.ignorable(step);
    }

    @Override
    public void pending(String step) {
        crossReference.pending(step);
        delegate.pending(step);
    }

    @Override
    public void notPerformed(String step) {
        crossReference.notPerformed(step);
        delegate.notPerformed(step);
    }

    @Override
    public void failed(String step, Throwable cause) {
        crossReference.failed(step, cause);
        delegate.failed(step, cause);
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        crossReference.failedOutcomes(step, table);
        delegate.failedOutcomes(step, table);
    }

    @Override
    public void dryRun() {
        crossReference.dryRun();    
        delegate.dryRun();
    }

    @Override
    public void replay() {
    }
 }
