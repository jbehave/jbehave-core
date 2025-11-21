package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;

/**
 * A Step represents a runnable portion of a Scenario, which matches methods annotated in {@link CandidateSteps}
 * instances.
 */
public interface Step {

    StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened);

    StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened);

    /**
     * Returns formatted step representation with parameters resolution.
     *
     * @param keywords the keywords to use for formatting
     * @return the formatted string representation of the step
     */
    String asString(Keywords keywords);

    /**
     * Returns plain step representation as its written in stories or composite steps without
     * parameter resolution.
     *
     * @param keywords the keywords to use for formatting
     * @return the plain string representation of the step
     */
    String asPlainString(Keywords keywords);

    List<Step> getComposedSteps();
}
