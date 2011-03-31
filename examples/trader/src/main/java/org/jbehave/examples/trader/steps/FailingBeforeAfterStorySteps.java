package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStory;

/**
 * Failing steps executed before and after each story
 */
public class FailingBeforeAfterStorySteps {

    @BeforeStory
    public void beforeStory() {
        throw new RuntimeException("Failure before story");
    }

    @AfterStory
    public void afterStory() {
        throw new RuntimeException("Failure after story");
    }


}
