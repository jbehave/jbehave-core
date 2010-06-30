package org.jbehave.core.steps;

import static org.jbehave.core.steps.AbstractStepResult.failed;
import static org.jbehave.core.steps.AbstractStepResult.notPerformed;
import static org.jbehave.core.steps.AbstractStepResult.pending;
import static org.jbehave.core.steps.AbstractStepResult.successful;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jbehave.core.reporters.StoryReporter;
import org.junit.Test;


public class StepResultBehaviour {
    
    @Test
    public void shouldDescribeItselfToAReporter() {
        // Given
        IllegalStateException exception = new IllegalStateException();
        StoryReporter reporter = mock(StoryReporter.class);

        // When
        String successful = "Given that a step is pending or failing";
        successful(successful).describeTo(reporter);
        String pending = "When a step is performed";
        pending(pending).describeTo(reporter);
        String notPerformed = "Then the step should describe itself properly to reporters";
        notPerformed(notPerformed).describeTo(reporter);
        String failed = "And any errors should appear at the end of the story";
        failed(failed, exception).describeTo(reporter);

        // Then
        verify(reporter).successful(successful);
        verify(reporter).pending(pending);
        verify(reporter).notPerformed(notPerformed);
        verify(reporter).failed(failed, exception);
    }
    
    @Test
    public void shouldDescribeItselfWithParameterValuesWhenAvailable() {
        // Given
        IllegalStateException exception = new IllegalStateException();
        StoryReporter reporter = mock(StoryReporter.class);

        // When
        String successful = "Given that a step is pending or failing";
        successful("Given that a step is $pending or $failing").withParameterValues(successful).describeTo(reporter);
        String pending = "When a step is performed";
        pending("When a step is $performed").withParameterValues(pending).describeTo(reporter);
        String notPerformed = "Then the step should describe itself properly to reporters";
        notPerformed("Then the step should $describe itself properly to reporters").withParameterValues(notPerformed).describeTo(reporter);
        String failed = "And any errors should appear at the end of the story";
        failed("And any errors should $appear at the end of the story", exception).withParameterValues(failed).describeTo(reporter);

        // Then
        verify(reporter).successful(successful);
        verify(reporter).pending(pending);
        verify(reporter).notPerformed(notPerformed);
        verify(reporter).failed(failed, exception);
    }
    
}
