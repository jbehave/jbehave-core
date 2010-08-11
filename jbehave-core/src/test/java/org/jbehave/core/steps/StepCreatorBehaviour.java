package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.steps.AbstractStepResult.Failed;
import org.junit.Test;


public class StepCreatorBehaviour {
    
    @Test(expected = BeforeOrAfterFailed.class )
    public void shouldHandleTargetInvocationFailureInBeforeOrAfterStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = new StepCreator(stepsInstance, new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aFailingMethod");
        stepCreator.createBeforeOrAfterStep(method).perform();

        // Then ... fail as expected
    }

    @Test(expected = BeforeOrAfterFailed.class )
    public void shouldHandleFailureInBeforeOrAfterStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = new StepCreator(stepsInstance, new SilentStepMonitor());

        // When
        Method method = null;
        stepCreator.createBeforeOrAfterStep(method).perform();

        // Then ... fail as expected
    }

    @Test
    public void shouldHandleTargetInvocationFailureInParametrisedStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = new StepCreator(stepsInstance, new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aFailingMethod");
        StepResult stepResult = stepCreator.createParametrisedStep(method, "When I fail", "I fail", null).perform();

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
    }

    @Test
    public void shouldHandleFailureInParametrisedStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = new StepCreator(stepsInstance, new SilentStepMonitor());

        // When
        Method method = null;
        StepResult stepResult = stepCreator.createParametrisedStep(method, "When I fail", "I fail", null).perform();

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
    }

}
