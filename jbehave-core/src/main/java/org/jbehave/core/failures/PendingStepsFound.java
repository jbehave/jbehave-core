package org.jbehave.core.failures;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.steps.StepCreator.PendingStep;

/**
 * Thrown when a pending step is found
 */
@SuppressWarnings("serial")
public class PendingStepsFound extends UUIDExceptionWrapper {

    public PendingStepsFound(List<PendingStep> steps) {
        super(asString(steps));
    }

    private static String asString(List<PendingStep> steps) {
        List<String> list = new ArrayList<>();
        for (PendingStep step : steps) {
            list.add(step.stepAsString());
        }
        return list.toString();
    }

}
