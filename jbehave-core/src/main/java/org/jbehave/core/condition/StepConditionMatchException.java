package org.jbehave.core.condition;

public class StepConditionMatchException extends Exception {

    private static final long serialVersionUID = -4614724300184207452L;

    public StepConditionMatchException(Throwable cause) {
        super(cause);
    }

    public StepConditionMatchException(String message) {
        super(message);
    }

}
