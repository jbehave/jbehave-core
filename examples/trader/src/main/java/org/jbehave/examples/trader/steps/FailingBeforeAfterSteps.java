package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;

/**
 * Failing steps executed before/after scenario/story/stories
 */
public class FailingBeforeAfterSteps {

    @BeforeScenario
    public void beforeScenario() {
        throw new RuntimeException("Failure before scenario");
    }

    @AfterScenario
    public void afterScenario() {
        throw new RuntimeException("Failure after scenario");
    }

    @BeforeStory
    public void beforeStory() {
        throw new RuntimeException("Failure before story");
    }

    @AfterStory
    public void afterStory() {
        throw new RuntimeException("Failure after story");
    }

    @BeforeStories
    public void beforeStories() {
        throw new RuntimeException("Failure before stories");
    }

    @AfterStories
    public void afterStories() {
        throw new RuntimeException("Failure after stories");
    }

}
