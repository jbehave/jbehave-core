package org.jbehave.examples.trader.spring;

import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.CONSOLE;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.HTML;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.TXT;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.XML;

import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.spring.scenarios.WildcardSearch;
import org.jbehave.scenario.Configuration;
import org.jbehave.scenario.MostUsefulConfiguration;
import org.jbehave.scenario.RunnableScenario;
import org.jbehave.scenario.ScenarioRunner;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.PrefixCapturingPatternBuilder;
import org.jbehave.scenario.parser.ScenarioDefiner;
import org.jbehave.scenario.parser.ScenarioNameResolver;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.FilePrintStreamFactory;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.scenario.reporters.ScenarioReporterBuilder;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.StepsFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Run JBehave scenarios via Spring JUnit4 integration.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jbehave/examples/trader/spring/steps.xml" })
public class SpringTraderRunner {

	@Autowired
    private BeforeAfterSteps beforeAfterSteps;

    @Autowired
    private TraderSteps traderSteps;

	@Test
	public void run() throws Throwable {
		ScenarioRunner runner = new ScenarioRunner();
		CandidateSteps[] candidateSteps = candidateSteps();
		for (Class<? extends RunnableScenario> scenarioClass : scenarioClasses()) {
			Configuration configuration = configure(scenarioClass);
			runner.run(scenarioClass, configuration, candidateSteps);
		}
	}

	public CandidateSteps[] candidateSteps() {
        StepsConfiguration configuration = new StepsConfiguration();
        configuration.usePatternBuilder(new PrefixCapturingPatternBuilder("%")); // use '%' instead of '$' to identify parameters        
        return new StepsFactory(configuration)
				.createCandidateSteps(traderSteps, beforeAfterSteps);
	}

	@SuppressWarnings("unchecked")
	public Class<? extends RunnableScenario>[] scenarioClasses() {
		return new Class[] { WildcardSearch.class };
	}

	public Configuration configure(final
			Class<? extends RunnableScenario> scenarioClass) {
		final ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver(
				".scenario");
		return new MostUsefulConfiguration() {
			public ScenarioDefiner forDefiningScenarios() {
				return new ClasspathScenarioDefiner(resolver,
						new PatternScenarioParser(keywords()));
			}

			@Override
			public ScenarioReporter forReportingScenarios() {
				return new ScenarioReporterBuilder(new FilePrintStreamFactory(
						scenarioClass, resolver)).with(CONSOLE).with(TXT).with(
						HTML).with(XML).build();
			}
		};
	}
}
