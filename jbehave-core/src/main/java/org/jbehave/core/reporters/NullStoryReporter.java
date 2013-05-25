package org.jbehave.core.reporters;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;

/**
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null-object</a> implementation of 
 * {@link StoryReporter}. Users can subclass it and can override only the method that they
 * are interested in.
 * </p>
 */
public class NullStoryReporter implements StoryReporter {

    public void beforeStep(String step) {
    }

    public void successful(String step) {
    }

    public void ignorable(String step) {
    }

    public void pending(String step) {
    }

    public void notPerformed(String step) {
    }

    public void failed(String step, Throwable cause) {
    }

    public void failedOutcomes(String step, OutcomesTable table) {
    }

    public void storyNotAllowed(Story story, String filter) {
    }

    public void beforeStory(Story story, boolean givenStory) {
    }

    public void storyCancelled(Story story, StoryDuration storyDuration) {
    }

    public void afterStory(boolean givenStory) {
    }

    public void narrative(final Narrative narrative) {
    }

    public void lifecyle(Lifecycle lifecycle) {
    }

    public void givenStories(GivenStories givenStories) {
    }

    public void givenStories(List<String> storyPaths) {
    }

    public void beforeScenario(String title) {
    }

    public void scenarioNotAllowed(Scenario scenario, String filter) {
    }

    public void scenarioMeta(Meta meta) {
    }

    public void afterScenario() {
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
    }

    public void example(Map<String, String> tableRow) {
    }

    public void afterExamples() {
    }

    public void dryRun() {
    }

    public void pendingMethods(List<String> methods) {
    }

    public void restarted(String step, Throwable cause) {
    }


}
