package org.jbehave.core.steps;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;

public interface StepResult {
	
	enum Type {
		FAILED,
		NOT_PERFORMED,
		PENDING, 
		SUCCESSFUL,
		SILENT,
		IGNORABLE,
		SKIPPED
	}

	String parametrisedStep();

	StepResult withParameterValues(String parametrisedStep);

    StepResult setTimings(Timer timer);

	void describeTo(StoryReporter reporter);

	UUIDExceptionWrapper getFailure();

}