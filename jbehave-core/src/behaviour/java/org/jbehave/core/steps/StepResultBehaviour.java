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
        IllegalStateException exception = new IllegalStateException();
        StoryReporter reporter = mock(StoryReporter.class);

        successful("Given that a step is pending or failing").describeTo(reporter);
        pending("When a step is performed").describeTo(reporter);
        notPerformed("Then the step should describe itself properly to reporters").describeTo(reporter);
        failed("And any errors should appear at the end of the core", exception).describeTo(reporter);
        
        verify(reporter).successful("Given that a step is pending or failing");
        verify(reporter).pending("When a step is performed");
        verify(reporter).notPerformed("Then the step should describe itself properly to reporters");
        verify(reporter).failed("And any errors should appear at the end of the core", exception);
    }
}
