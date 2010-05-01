package org.jbehave.examples.trader.scenarios;

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.MostUsefulConfiguration;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.ScenarioDefiner;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.scenario.steps.PrintStreamStepMonitor;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.StepsFactory;

public class AndStep extends JUnitScenario {

	public AndStep() {
		useConfiguration(new MostUsefulConfiguration() {
			@Override
			public ScenarioDefiner forDefiningScenarios() {
				return new ClasspathScenarioDefiner(
						new UnderscoredCamelCaseResolver(".scenario"),
						new PatternScenarioParser(keywords()));
			}

			@Override
			public ScenarioReporter forReportingScenarios() {
				return new PrintStreamScenarioReporter();
			}

		});

		StepsConfiguration configuration = new StepsConfiguration();
        configuration.useMonitor(new PrintStreamStepMonitor());
		addSteps(new StepsFactory(configuration).createCandidateSteps(new AndSteps()));
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
