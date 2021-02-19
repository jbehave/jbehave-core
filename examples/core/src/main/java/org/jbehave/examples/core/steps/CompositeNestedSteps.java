package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Then;

public class CompositeNestedSteps {

    @Then("all buttons are enabled")
    @Composite(steps = {
        "Then all left buttons are enabled",
        "Then all top buttons are enabled" }
    )
    public void all() {
    }

    @Then("all left buttons are enabled")
    @Composite(steps = {
        "Then first left button is enabled",
        "Then second left button is enabled" }
    )
    public void leftAll() {
    }

    @Then("first left button is enabled")
    public void leftOne() {
    }

    @Then("second left button is enabled")
    public void leftTwo() {
    }

    @Then("all top buttons are enabled")
    @Composite(steps = {
        "Then first top button is enabled",
        "Then second top button is enabled" }
    )
    public void topAll() {
    }

    @Then("first top button is enabled")
    public void topOne() {
    }

    @Then("second top button is enabled")
    public void topTwo() {
    }
}
