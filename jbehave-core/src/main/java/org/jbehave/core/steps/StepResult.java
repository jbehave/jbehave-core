package org.jbehave.core.steps;

import org.jbehave.core.reporters.StoryReporter;

public interface StepResult {

	String parametrisedStep();

	StepResult withParameterValues(String parametrisedStep);

	void describeTo(StoryReporter reporter);

	Throwable getFailure();

}