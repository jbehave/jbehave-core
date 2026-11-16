package org.jbehave.core.reporters;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import org.jbehave.core.model.Step;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class ConcurrentStoryReporterBehaviour {

    @Test
    void shouldDelayStepAttributesWhenMultithreading() {
        StoryReporter delegate = mock(StoryReporter.class);
        ConcurrentStoryReporter reporter = new ConcurrentStoryReporter(new NullStoryReporter(),
                singletonList(delegate), true);
        Step step = new Step(StepExecutionType.EXECUTABLE, "When something");
        Map<String, String> attributes = singletonMap("name", "value");

        reporter.beforeStep(step);
        reporter.stepAttributes(attributes);
        reporter.successful("When something");
        verifyNoInteractions(delegate);

        reporter.invokeDelayed();

        InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).beforeStep(step);
        inOrder.verify(delegate).stepAttributes(attributes);
        inOrder.verify(delegate).successful("When something");
    }

    @Test
    void shouldReportStepAttributesImmediatelyWhenNotMultithreading() {
        StoryReporter delegate = mock(StoryReporter.class);
        ConcurrentStoryReporter reporter = new ConcurrentStoryReporter(new NullStoryReporter(),
                singletonList(delegate), false);
        Map<String, String> attributes = singletonMap("name", "value");

        reporter.stepAttributes(attributes);

        verify(delegate).stepAttributes(attributes);
    }
}
