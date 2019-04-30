package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;

/**
 * A Step represents a runnable portion of a Scenario, which matches methods annotated in {@link CandidateSteps} instances.
 */
public interface Step {

    StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened);

    StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened);

    String asString(Keywords keywords);

    List<Step> getComposedSteps();
}
