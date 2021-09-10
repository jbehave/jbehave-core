package org.jbehave.examples.executablejar.steps;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class MySteps {
    @When("I run this as executable jar")
    @Alias("this")
    public void whenIRunThisAsExecutableJar() {
        // we don't do anything
    }

    @Then("this story should run")
    @Alias("that")
    public void thenThisStoryShouldRun() {
        // we don't do anything
    }
}
