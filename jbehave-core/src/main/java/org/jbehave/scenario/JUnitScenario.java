package org.jbehave.scenario;

import java.util.List;

import junit.framework.TestCase;

import org.jbehave.scenario.steps.CandidateSteps;
import org.junit.Test;

/**
 * <p>
 * Scenario decorator that add supports for running scenarios as <a
 * href="http://junit.org">JUnit</a> tests. Both JUnit 4.x (via @Test
 * annotation) and JUnit 3.8.x (via TestCase inheritance) are supported.
 * </p>
 * <p>
 * Users requiring JUnit support will extends this class instead of
 * {@link AbstractScenario}, while providing the same dependencies and following
 * the same Scenario specification logic as described in
 * {@link AbstractScenario}. The only difference in the dependencies provided is
 * that the scenario class is automatically set to the one being implemented by
 * the user, ie the concrete decorator class.
 * </p>
 * 
 * @see AbstractScenario
 */
public abstract class JUnitScenario extends TestCase implements RunnableScenario {

    private final Class<? extends JUnitScenario> decoratorClass = this.getClass();
    private final RunnableScenario delegate;

    public JUnitScenario(CandidateSteps... candidateSteps) {
        this.delegate = new JUnitScenarioDelegate(decoratorClass, candidateSteps);
    }

    public JUnitScenario(Configuration configuration, CandidateSteps... candidateSteps) {
        this.delegate = new JUnitScenarioDelegate(decoratorClass, configuration, candidateSteps);
    }

    public JUnitScenario(ScenarioRunner scenarioRunner, CandidateSteps... candidateSteps) {
        this.delegate = new JUnitScenarioDelegate(decoratorClass, scenarioRunner, candidateSteps);
    }

    public JUnitScenario(ScenarioRunner scenarioRunner, Configuration configuration, CandidateSteps... candidateSteps) {
        this.delegate = new JUnitScenarioDelegate(decoratorClass, scenarioRunner, configuration, candidateSteps);
    }

    public JUnitScenario(RunnableScenario delegate) {
        this.delegate = delegate;
    }

    @Test
    public void runScenario() throws Throwable {
        this.delegate.runScenario();
    }
    
    public void useConfiguration(Configuration configuration) {
        this.delegate.useConfiguration(configuration);
    }
    
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    public void addSteps(CandidateSteps... steps) {
        this.delegate.addSteps(steps);
    }

    public List<CandidateSteps> getSteps() {
    	return delegate.getSteps();
    }
    
    public void generateStepdoc() {
    	this.delegate.generateStepdoc();
	}

	/**
     * A JUnit 3-compatibile runnable method which simply delegates
     * {@link RunnableScenario#runScenario()}
     * 
     * @throws Throwable
     */
    public void testScenario() throws Throwable {
        runScenario();
    }

    public static class JUnitScenarioDelegate extends AbstractScenario {

        public JUnitScenarioDelegate(Class<? extends RunnableScenario> decoratorClass, CandidateSteps... candidateSteps) {
            super(decoratorClass, candidateSteps);
        }

        public JUnitScenarioDelegate(Class<? extends RunnableScenario> decoratorClass, Configuration configuration,
                CandidateSteps... candidateSteps) {
            super(decoratorClass, configuration, candidateSteps);
        }

        public JUnitScenarioDelegate(Class<? extends RunnableScenario> decoratorClass, ScenarioRunner scenarioRunner,
                CandidateSteps... candidateSteps) {
            super(decoratorClass, scenarioRunner, candidateSteps);
        }

        public JUnitScenarioDelegate(Class<? extends RunnableScenario> decoratorClass, ScenarioRunner scenarioRunner,
                Configuration configuration, CandidateSteps... candidateSteps) {
            super(decoratorClass, scenarioRunner, configuration, candidateSteps);
        }

    }

}
