package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.CONSOLE;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.HTML;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.TXT;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.XML;

import org.jbehave.examples.trader.converters.TraderConverter;
import org.jbehave.examples.trader.model.Stock;
import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.persistence.TraderPersister;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.PropertyBasedConfiguration;
import org.jbehave.scenario.RunnableScenario;
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
import org.jbehave.scenario.steps.ParameterConverters;
import org.jbehave.scenario.steps.SilentStepMonitor;
import org.jbehave.scenario.steps.StepMonitor;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.StepsFactory;

public class TraderScenario extends JUnitScenario {

    private static ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver(".scenario");

    public TraderScenario(final Class<? extends RunnableScenario> scenarioClass) {
        super(new PropertyBasedConfiguration() {
            @Override
            public ScenarioDefiner forDefiningScenarios() {
                return new ClasspathScenarioDefiner(resolver, new PatternScenarioParser(keywords()));
            }

            @Override
            public ScenarioReporter forReportingScenarios() {
                return new ScenarioReporterBuilder(new FilePrintStreamFactory(scenarioClass, resolver))
                            .with(CONSOLE)
                            .with(TXT)
                            .with(HTML)
                            .with(XML)
                            .build();
            }

        });

        StepsConfiguration configuration = new StepsConfiguration();
        StepMonitor monitor = new SilentStepMonitor();
		configuration.useParameterConverters(new ParameterConverters(
        		monitor, new TraderConverter(mockTradePersister())));  // define converter for custom type Trader
        configuration.usePatternBuilder(new PrefixCapturingPatternBuilder("%")); // use '%' instead of '$' to identify parameters
        configuration.useMonitor(monitor);
        
        addSteps(createSteps(configuration));
    }

    protected CandidateSteps[] createSteps(StepsConfiguration configuration) {
        return new StepsFactory(configuration).createCandidateSteps(new TraderSteps(new TradingService()), new BeforeAfterSteps());
    }

    private TraderPersister mockTradePersister() {
        return new TraderPersister(new Trader("Mauro", asList(new Stock("STK1", 10.d))));
    }


}
