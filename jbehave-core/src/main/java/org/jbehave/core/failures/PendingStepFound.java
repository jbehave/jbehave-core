package org.jbehave.core.failures;

import org.jbehave.core.steps.CorrelatedException;

/**
 * Thrown when a pending step is found
 */
@SuppressWarnings("serial")
public class PendingStepFound extends CorrelatedException {

    public PendingStepFound(String step) {
        super(step);
    }

}
