package org.jbehave.examples.trader.stories;

import junit.framework.Assert;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.Steps;
import org.jbehave.examples.trader.TraderStory;

public class FailureFollowedByGivenStories extends TraderStory {

	public FailureFollowedByGivenStories() {
		addSteps(new InstanceStepsFactory(configuration(), new SandpitSteps()).createCandidateSteps());
	}

	public static class SandpitSteps extends Steps {

		@Given("I do nothing")
		public void doNothing() {
		}

		@Then("I fail")
		public void doFail() {
			Assert.fail("I failed!");
		}

		@Then("I pass")
		public void doPass() {
		}
	}

}
