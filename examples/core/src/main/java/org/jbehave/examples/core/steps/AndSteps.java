package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;

public class AndSteps {
	@Given("the wind blows")
	public void givenWindBlows() {
		System.err.println("given the wind blows");
	}

	@When("the wind blows")
	public void whenWindBlows() {
		System.err.println("when the wind blows");
	}

}