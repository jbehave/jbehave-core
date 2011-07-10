package org.jbehave.core.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.model.Meta;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.jbehave.core.steps.SomeSteps.methodFor;
import static org.jbehave.core.steps.StepCollector.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BeforeOrAfterStepBehaviour {
    @Test
    public void shouldPassMetaToStepCreatorWhenCreatingStepWithMeta() throws Exception {
        StepCreator stepCreator = mock(StepCreator.class);

        Method method = methodFor("aMethodWith");
        BeforeOrAfterStep beforeOrAfterStep = new BeforeOrAfterStep(Stage.BEFORE, method, stepCreator);

        Meta meta = mock(Meta.class);
        beforeOrAfterStep.createStepWith(meta);

        verify(stepCreator).createBeforeOrAfterStep(method, meta);
    }

    @Test
    public void shouldPassMetaToStepCreatorWhenCreatingStepUponOutcomeWithMeta() throws Exception {
        StepCreator stepCreator = mock(StepCreator.class);

        Method method = methodFor("aMethodWith");
        BeforeOrAfterStep beforeOrAfterStep = new BeforeOrAfterStep(Stage.AFTER, method, stepCreator);

        Meta meta = mock(Meta.class);
        boolean failureOccured = false;
        beforeOrAfterStep.createStepUponOutcome(failureOccured, meta);

        verify(stepCreator).createAfterStepUponOutcome(method, AfterScenario.Outcome.ANY, failureOccured, meta);
    }
}
