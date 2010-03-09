package org.jbehave.examples.trader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;

public class PriorityMatchingSteps {

    private String param;

    @Given("a step that has $param")
    public void has(String param){
        this.param = param;
    }
    
    @Given(value="a step that has exactly one $param", priority=1)
    public void hasExactlyOne(String param){
        this.param = param;
    }

    @Then("the parameter value is \"$param\"")
    public void theParamValue(String param){
       ensureThat(this.param, equalTo(param));
    }

}
