package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;

import java.util.Collections;
import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.junit.Test;

public class StepFinderBehaviour {

    private StepFinder finder = new StepFinder();
    
    @Test
    public void shouldFindStepdocs() throws Exception {
        MySteps mySteps = new MySteps();
        List<Stepdoc> stepdocs = finder.stepdocs(new InstanceStepsFactory(new MostUsefulConfiguration(), mySteps).createCandidateSteps());
        Collections.sort(stepdocs);
        assertThat(stepdocs.size(), equalTo(3));
        assertThatStepdocIs(stepdocs.get(0), "givenFoo", "givenFoo(java.lang.String)", "foo named $name", "Given", GIVEN, mySteps);
        assertThatStepdocIs(stepdocs.get(1), "whenFoo", "whenFoo(java.lang.String)", "foo named $name", "When", WHEN, mySteps);
        assertThatStepdocIs(stepdocs.get(2), "thenFoo", "thenFoo(java.lang.String)", "foo named $name", "Then", THEN, mySteps);        
    }
    
    private void assertThatStepdocIs(Stepdoc stepdoc, String methodName, String methodSignature, String pattern, String startingWord, StepType stepType, Object stepsInstance) {
        assertThat(stepdoc.getMethod().getName(), equalTo(methodName));
        assertThat(stepdoc.toString(), containsString(methodName));
        assertThat(stepdoc.getMethodSignature(), containsString(methodName));
        assertThat(stepdoc.getPattern(), equalTo(pattern));
        assertThat(stepdoc.toString(), containsString(pattern));
        assertThat(stepdoc.getStartingWord(), equalTo(startingWord));
        assertThat(stepdoc.toString(), containsString(startingWord));
        assertThat(stepdoc.getStepType(), equalTo(stepType));
        assertThat(stepdoc.toString(), containsString(stepType.toString()));
        assertThat(stepdoc.getStepsInstance(), equalTo(stepsInstance));
        assertThat(stepdoc.toString(), containsString(stepsInstance.getClass().getName()));
    }

    static class MySteps  {

        @Given("foo named $name")
        public void givenFoo(String name) {
        }

        @When("foo named $name")
        public void whenFoo(String name) {
        }

        @Then("foo named $name")
        public void thenFoo(String name) {
        }

    }
    

}
