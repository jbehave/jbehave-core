package org.jbehave.examples.trader.stories;

import junit.framework.Assert;
import org.jbehave.core.JUnitStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.StepsFactory;

public class FailureFollowedByGivenStories extends JUnitStory {

	public FailureFollowedByGivenStories() {
		addSteps(new StepsFactory().createCandidateSteps(new SandpitSteps()));
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
