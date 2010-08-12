package org.jbehave.core.failures;

/**
 * Thrown when a pending step is found
 */
@SuppressWarnings("serial")
public class PendingStepFound extends RuntimeException {

    public PendingStepFound(String step) {
        super(step);
    }

}
