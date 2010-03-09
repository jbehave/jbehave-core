package org.jbehave.scenario;

import org.jbehave.scenario.definition.KeyWords;
import org.jbehave.scenario.errors.ErrorStrategy;
import org.jbehave.scenario.errors.ErrorStrategyInWhichWeTrustTheReporter;
import org.jbehave.scenario.errors.PendingErrorStrategy;
import org.jbehave.scenario.i18n.I18nKeyWords;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.parser.ScenarioDefiner;
import org.jbehave.scenario.reporters.PassSilentlyDecorator;
import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;
import org.jbehave.scenario.reporters.PrintStreamStepdocReporter;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.scenario.reporters.StepdocReporter;
import org.jbehave.scenario.steps.DefaultStepdocGenerator;
import org.jbehave.scenario.steps.StepCreator;
import org.jbehave.scenario.steps.StepdocGenerator;
import org.jbehave.scenario.steps.UnmatchedToPendingStepCreator;

/**
 * The default configuration used by {@link ScenarioRunner}. Works for most
 * situations that users are likely to encounter. The default elements
 * configured are:
 * <ul>
 *   <li>{@link StepCreator}: new UnmatchedToPendingStepCreator()</li>
 *   <li>{@link ScenarioDefiner}: new ClasspathScenarioDefiner(new PatternScenarioParser(this))</li>
 *   <li>{@link ErrorStrategy}: ErrorStrategy.RETHROW</li>
 *   <li>{@link PendingErrorStrategy}: PendingErrorStrategy.PASSING</li>
 *   <li>{@link ScenarioReporter}: new PassSilentlyDecorator(new PrintStreamScenarioReporter())</li>
 *   <li>{@link KeyWords}: new I18nKeyWords()</li>
 *   <li>{@link StepdocGenerator}: new DefaultStepdocGenerator()</li>
 *   <li>{@link StepdocReporter}: new PrintStreamStepdocReporter(true)</li>
 * </ul>
 */
public class MostUsefulConfiguration implements Configuration {

	/**
	 * Provides pending steps where unmatched steps exist.
	 */
	public StepCreator forCreatingSteps() {
		return new UnmatchedToPendingStepCreator();
	}

	/**
	 * Defines scenarios by looking for a file named after the scenario and in
	 * the same package, using lower-case underscored name in place of the
	 * camel-cased name - so MyScenario.java maps to my_scenario.
	 */
	public ScenarioDefiner forDefiningScenarios() {
		return new ClasspathScenarioDefiner(new PatternScenarioParser(keywords()));
	}

	/**
	 * Handles errors by rethrowing them.
	 * 
	 * <p>
	 * If there are multiple scenarios in a single story definition, this could
	 * cause the story to stop after the first failing scenario.
	 * 
	 * <p>
	 * If you want different behaviour, you might want to look at the
	 * {@link ErrorStrategyInWhichWeTrustTheReporter}.
	 */
	public ErrorStrategy forHandlingErrors() {
		return ErrorStrategy.RETHROW;
	}

	/**
	 * Allows pending steps to pass, so that builds etc. will not fail.
	 * 
	 * <p>
	 * If you want to spot pending steps, you might want to look at
	 * {@link PendingStepStrategy.FAILING}, or alternatively at the
	 * PropertyBasedConfiguration which provides a mechanism for altering this
	 * behaviour in different environments.
	 */
	public PendingErrorStrategy forPendingSteps() {
		return PendingErrorStrategy.PASSING;
	}

	/**
	 * Reports failing or pending scenarios to System.out, while silently
	 * passing scenarios.
	 * 
	 * <p>
	 * If you want different behaviour, you might like to use the
	 * {@link PrintStreamScenarioReporter}, or look at the {@link PropertyBasedConfiguration}
	 * which provides a mechanism for altering this behaviour in different
	 * environments.
	 */
	public ScenarioReporter forReportingScenarios() {
		return new PassSilentlyDecorator(new PrintStreamScenarioReporter());
	}

	/**
	 * Provides the keywords in English
	 */
	public KeyWords keywords() {
		return new I18nKeyWords();
	}

	/**
	 * Generates stepdocs
	 */
	public StepdocGenerator forGeneratingStepdoc() {
		return new DefaultStepdocGenerator();
	}

	/**
	 * Reports stepdocs to {@link System.out}
	 */
	public StepdocReporter forReportingStepdoc() {
		return new PrintStreamStepdocReporter(true);
	}

}
