package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class PendingSteps {

    @Given("a step is annotated as pending")
    @Pending
    public void givenAStepIsAnnotatedAsPending() {
        // PENDING
    }

    @When("a step is annotated as pending")
    @Pending
    public void whenAStepIsAnnotatedAsPending() {
        // PENDING
    }

    @Then("a step is annotated as pending")
    @Pending
    public void thenAStepIsAnnotatedAsPending() {
        // PENDING
    }

}
