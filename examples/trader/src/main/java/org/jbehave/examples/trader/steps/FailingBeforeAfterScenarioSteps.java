package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;

/**
 * Failing steps executed before and after each scenario
 */
public class FailingBeforeAfterScenarioSteps {

    @BeforeScenario
    public void beforeScenario() {
        throw new RuntimeException("Failure before scenario");
    }

    @AfterScenario
    public void afterScenario() {
        throw new RuntimeException("Failure after scenario");
    }


}
