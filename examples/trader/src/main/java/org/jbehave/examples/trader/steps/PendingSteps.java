package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class PendingSteps {

    @Given("a step that has %param")
    @Pending
    public void has(String param){
    }
    
    @When("a step has exactly one %param")
    @Pending
    public void hasExactlyOne(String param){
    }

    @Then("the parameter value is \"%param\"")
    @Pending
    public void theParamValue(String param){
    }

}