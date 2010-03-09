package org.jbehave.scenario.errors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StepFailureBehaviour {
	
	@Test
	public void shouldAppendStepNameToFailure() {
		// Given
		Throwable cause = new IllegalArgumentException(
				"Can't we all just get along?");
		String stepAsString = "Given something that could never work";
		StepFailure failure = new StepFailure(stepAsString, cause);

		// When
		String message = failure.getMessage();

		// Then
		assertThat(message, equalTo(cause.getMessage() + "\nduring step: '"
				+ stepAsString + "'"));
	}

	@Test
	public void shouldKeepOriginalExceptionAsCause() {
		// Given
		Throwable originalCause = new IllegalArgumentException(
				"Can't we all just get along?");
		StepFailure decorator = new StepFailure(
				"Given something that could never work", originalCause);

		// When
		Throwable cause = decorator.getCause();

		// Then
		assertThat(cause, sameInstance(originalCause));
	}
}
