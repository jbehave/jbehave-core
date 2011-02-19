package org.jbehave.core.steps;

import org.jbehave.core.failures.UUIDExceptionWrapper;

/**
 * A Step represents a runnable portion of a Scenario, which matches methods annotated in {@link CandidateSteps} instances.
 */
public interface Step {

    StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened);

    StepResult doNotPerform();

}
