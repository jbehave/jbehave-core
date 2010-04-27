package org.jbehave.examples.trader.scenarios;

import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.CONSOLE;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.HTML;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.TXT;
import static org.jbehave.scenario.reporters.ScenarioReporterBuilder.Format.XML;

import org.jbehave.examples.trader.PriorityMatchingSteps;
import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.MostUsefulConfiguration;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.PrefixCapturingPatternBuilder;
import org.jbehave.scenario.parser.ScenarioDefiner;
import org.jbehave.scenario.parser.ScenarioNameResolver;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.FilePrintStreamFactory;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.scenario.reporters.ScenarioReporterBuilder;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.StepsFactory;

public class PriorityMatching extends JUnitScenario {

    private static ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver(".scenario");

    public PriorityMatching() {
        super(new MostUsefulConfiguration() {
            @Override
            public ScenarioDefiner forDefiningScenarios() {
                return new ClasspathScenarioDefiner(resolver, new PatternScenarioParser(keywords()));
            }
            
            @Override
            public ScenarioReporter forReportingScenarios() {
                return new ScenarioReporterBuilder(new FilePrintStreamFactory(PriorityMatching.class, resolver))
                            .outputTo("target/jbehave-reports").outputAsAbsolute(true)
                            .withDefaultFormats()
                            .with(CONSOLE)
                            .with(TXT)
                            .with(HTML)
                            .with(XML)
                            .build();
            }

        });

        StepsConfiguration configuration = new StepsConfiguration();
        configuration.usePatternBuilder(new PrefixCapturingPatternBuilder("$")); 
        addSteps(new StepsFactory(configuration).createCandidateSteps(new PriorityMatchingSteps()));

    }

}
