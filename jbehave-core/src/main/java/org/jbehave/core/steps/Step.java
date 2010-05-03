package org.jbehave.core.steps;

/**
 * A Step represents a runnable portion of a Scenario, which matches methods annotated in {@link Steps} class.
 */
public interface Step {

    StepResult perform();

    StepResult doNotPerform();

}
