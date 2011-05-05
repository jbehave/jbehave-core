package org.jbehave.hudson;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class MySteps {

	@Given("a test")
	public void aTest() {
	}
	
	@Given("a test with <param1>")
	public void aTest(@Named("param1") final String value) {
	}

	@When("a test is executed")
	public void aTestIsExecuted() {
	}

	@When("a test is executed with <param2>")
	public void aTestIsExecuted(@Named("param2") final String value) {
		if ("fail".equalsIgnoreCase(value)) {
			throw new RuntimeException("When failed");
		}
	}

	@When("a test fails")
	public void aTestFails() {
		throw new RuntimeException("Test failed");
	}

	@Then("a tester is pleased")
	public void aTesterIsPleased() {
	}
}