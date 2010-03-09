package org.jbehave.scenario.errors;

/**
 * Thrown when a scenario is not found by the classloader
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class ScenarioNotFoundException extends RuntimeException {

    public ScenarioNotFoundException(String message) {
        super(message);
    }

}
