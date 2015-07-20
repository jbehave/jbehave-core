package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.failures.IgnoringStepsFailure;

public class IgnoringSteps {
    
    @When("I ignore next steps")
    public void restartScenario() {
        throw new IgnoringStepsFailure("Please ignore next steps");
    }

    @Then("this step is ignored")
    public void scenarioTwoTimesExecuted() {
        throw new RuntimeException("This step should be ignored and not executed.");
    }

}
