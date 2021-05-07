package org.jbehave.core.failures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;

class StepFailureBehaviour {
    
    @Test
    void shouldAppendStepNameToFailure() {
        // Given
        Throwable cause = new IllegalArgumentException(
                "Can't we all just get along?");
        String stepAsString = "Given something that could never work";
        StepFailed failure = new StepFailed(stepAsString, cause);

        // When
        String message = failure.getMessage();

        // Then
        assertThat(message, equalTo("'"
                + stepAsString + "': " +cause.getMessage()));
    }

    @Test
    void shouldKeepOriginalExceptionAsCause() {
        // Given
        Throwable originalCause = new IllegalArgumentException(
                "Can't we all just get along?");
        StepFailed decorator = new StepFailed(
                "Given something that could never work", originalCause);

        // When
        Throwable cause = decorator.getCause();

        // Then
        assertThat(cause, sameInstance(originalCause));
    }
}
