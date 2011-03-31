package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.BeforeStories;

/**
 * Failing steps executed before and after all stories
 */
public class FailingBeforeAfterStoriesSteps {

    @BeforeStories
    public void beforeStories() {
        throw new RuntimeException("Failure before stories");
    }

    @AfterStories
    public void afterStories() {
        throw new RuntimeException("Failure after stories");
    }


}
