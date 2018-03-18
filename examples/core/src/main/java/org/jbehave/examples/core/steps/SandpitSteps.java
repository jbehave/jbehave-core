package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.steps.Steps;

public class SandpitSteps extends Steps {

	@Given("I do nothing")
	public void doNothing() {
	}

	@Then("I fail")
	public void doFail() {
		throw new AssertionError("I failed!");
	}

	@Then("I pass")
	public void doPass() {
	}
}