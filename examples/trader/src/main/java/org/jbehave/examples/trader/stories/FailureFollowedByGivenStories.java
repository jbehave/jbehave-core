package org.jbehave.examples.trader.stories;

import junit.framework.Assert;
import org.jbehave.core.JUnitStory;
import org.jbehave.core.MostUsefulStoryConfiguration;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.StepsFactory;

public class FailureFollowedByGivenStories extends JUnitStory {

	public FailureFollowedByGivenStories() {
        useConfiguration(new MostUsefulStoryConfiguration()
                .useStoryPathResolver(new UnderscoredCamelCaseResolver(".story"))
                .useStoryReporter(new PrintStreamOutput()));
		addSteps(new StepsFactory(new MostUsefulStepsConfiguration())
				.createCandidateSteps(new SandpitSteps()));
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
