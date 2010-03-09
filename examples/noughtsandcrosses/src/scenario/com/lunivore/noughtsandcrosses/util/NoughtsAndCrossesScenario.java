package com.lunivore.noughtsandcrosses.util;

import org.jbehave.scenario.PropertyBasedConfiguration;
import org.jbehave.scenario.Scenario;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;
import org.jbehave.scenario.reporters.ScenarioReporter;

import com.lunivore.noughtsandcrosses.steps.BeforeAndAfterSteps;
import com.lunivore.noughtsandcrosses.steps.GridSteps;

public abstract class NoughtsAndCrossesScenario extends Scenario {

	public NoughtsAndCrossesScenario() {
		this(new OAndXUniverse());
	}
	
	public NoughtsAndCrossesScenario(OAndXUniverse universe) {
        super(new PropertyBasedConfiguration() {
            @Override
            public ClasspathScenarioDefiner forDefiningScenarios() {
                return new ClasspathScenarioDefiner(new UnderscoredCamelCaseResolver(), new PatternScenarioParser(this));
            }
            @Override
            public ScenarioReporter forReportingScenarios() {
                return new PrintStreamScenarioReporter();
            }
        }, new GridSteps(universe), new BeforeAndAfterSteps(universe));
     }

}
