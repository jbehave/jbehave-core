package org.jbehave.examples.core.steps;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;

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
       assertThat(this.param, equalTo(param));
    }

}