package org.jbehave.scenario;

import org.jbehave.scenario.steps.CandidateSteps;

/**
 * <p>
 * Scenario represents the suggested entry point for the scenario developers. 
 * It's a simple extension of JUnitScenario but users can choose to extend
 * other abstract implementations of RunnableScenario.
 * </p>
 */
public abstract class Scenario extends JUnitScenario {

    public Scenario(CandidateSteps... candidateSteps) {
        super(candidateSteps);
    }
    
    public Scenario(Configuration configuration, CandidateSteps... candidateSteps) {
        super(configuration, candidateSteps);
    }

    public Scenario(ScenarioRunner scenarioRunner, Configuration configuration, CandidateSteps... candidateSteps) {
        super(scenarioRunner, configuration, candidateSteps);
    }

    public Scenario(RunnableScenario delegate) {
        super(delegate);
    }

}
