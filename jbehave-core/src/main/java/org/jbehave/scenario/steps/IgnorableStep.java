package org.jbehave.scenario.steps;


public class IgnorableStep implements Step {
    
    private final String step;

    public IgnorableStep(String step) {
        this.step = step;
    }

    public StepResult perform() {
        return StepResult.ignorable(getDescription());
    }


    public String getDescription() {
        return step;
    }


    public StepResult doNotPerform() {
        return StepResult.ignorable(getDescription());
    }

    @Override
    public String toString() {
    	return getClass().getSimpleName() + "[" + step + "]";
    }
}
