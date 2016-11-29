package org.jbehave.core.steps;

import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;

import static org.jbehave.core.steps.AbstractStepResult.failed;
import static org.jbehave.core.steps.AbstractStepResult.ignorable;
import static org.jbehave.core.steps.AbstractStepResult.comment;
import static org.jbehave.core.steps.AbstractStepResult.notPerformed;
import static org.jbehave.core.steps.AbstractStepResult.pending;
import static org.jbehave.core.steps.AbstractStepResult.skipped;
import static org.jbehave.core.steps.AbstractStepResult.successful;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class StepResultBehaviour {
    
    @Test
    public void shouldDescribeResultToReporter() {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);

        // When
        String successful = "Given that a step is pending or failing";
        successful(successful).describeTo(reporter);
        String pending = "When a step is performed";
        pending(pending).describeTo(reporter);
        PendingStepFound pendingStepFound = new PendingStepFound(pending);
        pending(pending, pendingStepFound).describeTo(reporter);
        String notPerformed = "Then the step should describe itself properly to reporters";
        notPerformed(notPerformed).describeTo(reporter);
        String ignorable = "!-- Then ignore me";
        ignorable(ignorable).describeTo(reporter);
        String comment = "!-- this is a comment";
        comment(comment).describeTo(reporter);
        String failed = "And any errors should appear at the end of the story";
        UUIDExceptionWrapper cause = new UUIDExceptionWrapper(new IllegalStateException());
        failed(failed, cause).describeTo(reporter);
        String failedOutcomes = "And outcomes failed";
        OutcomesTable outcomesTable = new OutcomesTable();
        failed(failedOutcomes, new UUIDExceptionWrapper(new OutcomesFailed(outcomesTable))).describeTo(reporter);
        skipped().describeTo(reporter);

        // Then
        verify(reporter).successful(successful);
        verify(reporter, times(2)).pending(pending);
        verify(reporter).notPerformed(notPerformed);
        verify(reporter).ignorable(ignorable);
        verify(reporter).comment(comment);
        verify(reporter).failed(failed, cause);
        verify(reporter).failedOutcomes(failedOutcomes, outcomesTable);
    }
    
    @Test
    public void shouldDescribeResultToReporterWithParameterValuesWhenAvailable() {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);

        // When
        String successful = "Given that a step is pending or failing";
        successful("Given that a step is $pending or $failing").withParameterValues(successful).describeTo(reporter);
        String pending = "When a step is performed";
        pending("When a step is $performed").withParameterValues(pending).describeTo(reporter);
        String notPerformed = "Then the step should describe itself properly to reporters";
        notPerformed("Then the step should $describe itself properly to reporters").withParameterValues(notPerformed).describeTo(reporter);
        String failed = "And any errors should appear at the end of the story";
        UUIDExceptionWrapper cause = new UUIDExceptionWrapper(new IllegalStateException());
        failed("And any errors should $appear at the end of the story", cause).withParameterValues(failed).describeTo(reporter);

        // Then
        verify(reporter).successful(successful);
        verify(reporter).pending(pending);
        verify(reporter).notPerformed(notPerformed);
        verify(reporter).failed(failed, cause);
    }

    @Test
    public void shouldDescribeResultToString() {
        // Given        
        String stepPattern = "Given that a step is $pending or $failing";
        String successful = "Given that a step is pending or failing";
        
        // When
        StepResult resultWithoutParameterValues = successful(stepPattern);
        StepResult resultWithParameterValues = successful(stepPattern).withParameterValues(successful);

        // Then
        assertThat(resultWithoutParameterValues.toString(), containsString(stepPattern));
        assertThat(resultWithParameterValues.toString(), containsString(successful));
    }
}
