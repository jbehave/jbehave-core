package org.jbehave.core.steps;

import static org.jbehave.core.steps.AbstractStepResult.pending;


public class PendingStep implements Step {
    
    private final String step;

    public PendingStep(String step) {
        this.step = step;
    }

    public StepResult perform() {
        return pending(getDescription());
    }


    public String getDescription() {
        return step;
    }


    public StepResult doNotPerform() {
        return pending(getDescription());
    }

    @Override
    public String toString() {
    	return getClass().getSimpleName() + "[" + step + "]";
    }
}
