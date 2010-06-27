package org.jbehave.examples.trader.stories;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InstanceStepsFactory;

public class PriorityMatching extends JUnitStory {

    public PriorityMatching() {
        addSteps(new InstanceStepsFactory(configuration(), new PriorityMatchingSteps()).createCandidateSteps());
    }
    
    public static class PriorityMatchingSteps {

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

}
