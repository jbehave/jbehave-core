package org.jbehave.core.steps;

import static org.jbehave.core.steps.AbstractStepResult.ignorable;


public class IgnorableStep implements Step {
    
    private final String step;

    public IgnorableStep(String step) {
        this.step = step;
    }

    public StepResult perform() {
        return ignorable(getDescription());
    }


    public String getDescription() {
        return step;
    }


    public StepResult doNotPerform() {
        return ignorable(getDescription());
    }

    @Override
    public String toString() {
    	return getClass().getSimpleName() + "[" + step + "]";
    }
}
