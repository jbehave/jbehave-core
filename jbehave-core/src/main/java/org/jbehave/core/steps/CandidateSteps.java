package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.configuration.Configuration;

/**
 * Interface providing the list of candidate steps, representing methods
 * annotated with @Given, @When or @Then, that can be matched. It also provides
 * the list of runnable steps, representing methods annotated with @BeforeStory,
 * @AfterStory, @BeforeScenario, @AfterScenario, that do not require any matching.
 */
public interface CandidateSteps {

	/**
	 * Returns the candidate steps that can be matched
	 * 
	 * @return The list of candidate steps
	 */
	List<CandidateStep> listCandidates();

	/**
	 * Returns the steps to run before each story, based on the given story
	 * status
	 * 
	 * @param givenStory
	 *            the boolean flag denoting if it's a given story
	 * @return The list of runnable steps
	 */
	List<Step> runBeforeStory(boolean givenStory);

	/**
	 * Returns the steps to run after each story, based on the given story
	 * status
	 * 
	 * @param givenStory
	 *            the boolean flag denoting if it's a given story
	 * @return The list of runnable steps
	 */
	List<Step> runAfterStory(boolean givenStory);

	/**
	 * Returns the steps to run before each scenario
	 * 
	 * @return The list of runnable steps
	 */
	List<Step> runBeforeScenario();

	/**
	 * Returns the steps to run after each scenario
	 * 
	 * @return The list of runnable steps
	 */
	List<Step> runAfterScenario();

	/**
	 * Returns the configuration
	 * 
	 * @return The Configuration
	 */
	Configuration configuration();

}
