package org.jbehave.examples.trader.stories;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.MostUsefulStoryConfiguration;
import org.jbehave.core.StoryConfiguration;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.StepsConfiguration;
import org.jbehave.core.steps.StepsFactory;

public class AndStep extends JUnitStory {

	public AndStep() {
		StoryConfiguration storyConfiguration = new MostUsefulStoryConfiguration();
		storyConfiguration
				.useStoryPathResolver(new UnderscoredCamelCaseResolver(".story"));
		storyConfiguration.useStoryReporter(new PrintStreamOutput());
		useConfiguration(storyConfiguration);
		StepsConfiguration configuration = new MostUsefulStepsConfiguration();
		//configuration.useMonitor(new PrintStreamStepMonitor());
		addSteps(new StepsFactory(configuration)
				.createCandidateSteps(new AndSteps()));
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
