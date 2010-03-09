package org.jbehave.scenario;

import org.jbehave.scenario.definition.KeyWords;
import org.jbehave.scenario.errors.ErrorStrategy;
import org.jbehave.scenario.errors.PendingErrorStrategy;
import org.jbehave.scenario.parser.ScenarioDefiner;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.scenario.reporters.StepdocReporter;
import org.jbehave.scenario.steps.StepCreator;
import org.jbehave.scenario.steps.StepdocGenerator;

/**
 * Provides the configuration used by the {@link ScenarioRunner} and is injected
 * in the {@link Scenario} to customise its runtime properties.
 * 
 * The configuration may change dynamically, so any other class wishing to use this
 * should store the whole configuration, and use the respective parts of it at
 * runtime, rather than attempting to store any part of it when the
 * configuration is provided.
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface Configuration {

	ScenarioDefiner forDefiningScenarios();

	ScenarioReporter forReportingScenarios();

	PendingErrorStrategy forPendingSteps();

	StepCreator forCreatingSteps();

	ErrorStrategy forHandlingErrors();

	KeyWords keywords();

	StepdocGenerator forGeneratingStepdoc();

	StepdocReporter forReportingStepdoc();

}
