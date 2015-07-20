package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.failures.IgnoringStepsFailure;

public class IgnoringSteps {

    @Given("a successful step")
    public void givenASuccessfulStep() {
    }

    @When("ignore steps failure occurs")
    public void whenIgnoringStepsFailureOccurs() {
        throw new IgnoringStepsFailure("Please ignore next steps");
    }

    @Then("step is ignored")
    public void thenStepIsIgnored() {
        throw new RuntimeException("This step should be ignored and not executed.");
    }

}
