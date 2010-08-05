package org.jbehave.examples.trader.steps;

import junit.framework.Assert;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.steps.Steps;

public class SandpitSteps extends Steps {

	@Given("I do nothing")
	public void doNothing() {
	}

	@Then("I fail")
	public void doFail() {
		Assert.fail("I failed!");
	}

	@Then("I pass")
	public void doPass() {
	}
}