package org.jbehave.scenario.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jbehave.scenario.reporters.ScenarioReporter;
import org.junit.Test;


public class StepResultBehaviour {
    
    @Test
    public void shouldDescribeItselfToAReporter() {
        IllegalStateException exception = new IllegalStateException();
        ScenarioReporter reporter = mock(ScenarioReporter.class);

        StepResult.success("Given that a step is pending or failing").describeTo(reporter);
        StepResult.pending("When a step is performed").describeTo(reporter);
        StepResult.notPerformed("Then the step should describe itself properly to reporters").describeTo(reporter);
        StepResult.failure("And any errors should appear at the end of the scenario", exception).describeTo(reporter);
        
        verify(reporter).successful("Given that a step is pending or failing");
        verify(reporter).pending("When a step is performed");
        verify(reporter).notPerformed("Then the step should describe itself properly to reporters");
        verify(reporter).failed("And any errors should appear at the end of the scenario", exception);
    }
}
