package org.jbehave.core.failures;

@SuppressWarnings("serial")
public class PendingStepFound extends AssertionError {

    public PendingStepFound(String step) {
        super(step);
    }

}
