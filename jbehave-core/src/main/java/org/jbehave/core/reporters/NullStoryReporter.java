package org.jbehave.core.reporters;

import org.jbehave.core.model.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null-object</a> implementation of 
 * {@link StoryReporter}.   Users can subclass it and can override only the method that they
 * are interested in.
 * </p>
 */
public class NullStoryReporter implements StoryReporter {

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

    public void beforeStory(Story story, boolean givenStory) {
    }

    public void narrative(final Narrative narrative) {
    }

    public void storyNotAllowed(Story story, String filter) {
    }

    public void afterStory(boolean givenStory) {
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

}
