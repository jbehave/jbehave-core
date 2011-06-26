package org.jbehave.core.steps;

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
        BeforeOrAfterStep beforeOrAfterStep = new BeforeOrAfterStep(Stage.BEFORE, method, String.class, mock(InjectableStepsFactory.class), stepCreator);

        Meta meta = mock(Meta.class);
        beforeOrAfterStep.createStepWith(meta);

        verify(stepCreator).createBeforeOrAfterStepWithMeta(method, meta);
    }
}
