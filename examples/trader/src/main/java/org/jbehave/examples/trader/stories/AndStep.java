package org.jbehave.examples.trader.stories;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.BeforeAfterSteps;

public class AndStep extends JUnitStory {

	public AndStep() {
		addSteps(new InstanceStepsFactory(configuration(), new AndSteps(), new BeforeAfterSteps()).createCandidateSteps());
	}

	public static class AndSteps {
		@Given("the wind blows")
		public void givenWindBlows() {
			System.err.println("given the wind blows");
		}

		@When("the wind blows")
		public void whenWindBlows() {
			System.err.println("when the wind blows");
		}

	}
}
