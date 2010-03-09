package org.jbehave.scenario;

import java.util.List;

import org.jbehave.scenario.steps.CandidateSteps;

/**
 * <p>
 * Scenario represents the main interface to run a scenario.
 * </p>
 * <p>
 * Typically users will need to extend an abstract implementation, such as
 * {@link AbstractScenario} or a decorator scenarios, such as
 * {@link JUnitScenario}, which also provide support for test frameworks.
 * </p>
 * 
 * @see AbstractScenario
 * @see JUnitScenario
 */
public interface RunnableScenario {

    void runScenario() throws Throwable;

    void useConfiguration(Configuration configuration);
    
    Configuration getConfiguration();
    
    void addSteps(CandidateSteps... steps);

    List<CandidateSteps> getSteps();

	void generateStepdoc();

}
