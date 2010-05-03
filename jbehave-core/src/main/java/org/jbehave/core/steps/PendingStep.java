package org.jbehave.core.steps;


public class PendingStep implements Step {
    
    private final String step;

    public PendingStep(String step) {
        this.step = step;
    }

    public StepResult perform() {
        return StepResult.pending(getDescription());
    }


    public String getDescription() {
        return step;
    }


    public StepResult doNotPerform() {
        return StepResult.pending(getDescription());
    }

    @Override
    public String toString() {
    	return getClass().getSimpleName() + "[" + step + "]";
    }
}
