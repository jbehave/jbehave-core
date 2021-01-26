package org.jbehave.core.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.jbehave.core.steps.SomeSteps.methodFor;
import static org.jbehave.core.steps.StepCollector.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BeforeOrAfterStepBehaviour {
    @Test
    void shouldPassMetaToStepCreatorWhenCreatingStepWithMeta() throws Exception {
        StepCreator stepCreator = mock(StepCreator.class);

        Method method = methodFor("aMethodWith");
        BeforeOrAfterStep beforeOrAfterStep = new BeforeOrAfterStep(Stage.BEFORE, method, stepCreator);

        Meta meta = mock(Meta.class);
        beforeOrAfterStep.createStepWith(meta);

        verify(stepCreator).createBeforeOrAfterStep(method, meta);
    }

    @Test
    void shouldPassMetaToStepCreatorWhenCreatingStepUponOutcomeWithMeta() throws Exception {
        StepCreator stepCreator = mock(StepCreator.class);

        Method method = methodFor("aMethodWith");
        BeforeOrAfterStep beforeOrAfterStep = new BeforeOrAfterStep(Stage.AFTER, method, stepCreator);

        Meta meta = mock(Meta.class);
        beforeOrAfterStep.createStepUponOutcome(meta);

        verify(stepCreator).createAfterStepUponOutcome(method, AfterScenario.Outcome.ANY, meta);
    }
}
