package org.jbehave.examples.core.steps;

import static org.junit.Assert.assertEquals;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.RestartingStoryFailure;

public class RestartingSteps {
	private int restartingScenario;
	private int restartingStory;

	@When("I restart scenario")
	public void restartScenario(){
		if ( restartingScenario < 1 ){
			restartingScenario++;
			throw new RestartingScenarioFailure("Restarting scenario: "+restartingScenario);
		}
	}
	
	@Then("scenario has been executed two times")
    public void scenarioTwoTimesExecuted() {
        assertEquals(1, restartingScenario);
    }

	@When("I restart story")
	public void restartStory(){
		if ( restartingStory < 1 ){
			restartingStory++;
			throw new RestartingStoryFailure("Restarting story: "+restartingStory);
		}
	}
	
    @Then("story has been executed two times")
    public void storyTwoTimesExecuted() {
        assertEquals(1, restartingStory);
    }
		
}

