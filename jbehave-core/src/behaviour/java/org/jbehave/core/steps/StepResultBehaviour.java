package org.jbehave.core.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jbehave.core.reporters.StoryReporter;
import org.junit.Test;


public class StepResultBehaviour {
    
    @Test
    public void shouldDescribeItselfToAReporter() {
        IllegalStateException exception = new IllegalStateException();
        StoryReporter reporter = mock(StoryReporter.class);

        StepResult.success("Given that a step is pending or failing").describeTo(reporter);
        StepResult.pending("When a step is performed").describeTo(reporter);
        StepResult.notPerformed("Then the step should describe itself properly to reporters").describeTo(reporter);
        StepResult.failure("And any errors should appear at the end of the core", exception).describeTo(reporter);
        
        verify(reporter).successful("Given that a step is pending or failing");
        verify(reporter).pending("When a step is performed");
        verify(reporter).notPerformed("Then the step should describe itself properly to reporters");
        verify(reporter).failed("And any errors should appear at the end of the core", exception);
    }
}
