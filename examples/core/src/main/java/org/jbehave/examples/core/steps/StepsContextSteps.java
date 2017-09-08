package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.FromContext;
import org.jbehave.core.annotations.When;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.ToContext;
import org.junit.Assert;


public class StepsContextSteps {

	@When("a variable with value $value is stored in steps context")
	@ToContext("myVariable")
	public String givenAVariable(String value){
		return value;
	}

	@Then("the steps context includes the value $value")
	public void thenTheContextIncludes(@FromContext("myVariable") String contextValue, String expectedValue){
		Assert.assertEquals(contextValue, expectedValue);
	}

}
