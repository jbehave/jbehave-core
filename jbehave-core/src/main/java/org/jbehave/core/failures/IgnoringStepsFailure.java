package org.jbehave.core.failures;

/**
 * Runtime exception thrown to indicate that the current and following steps should be ignored
 */
@SuppressWarnings("serial")
public class IgnoringStepsFailure extends RuntimeException {
    
    public IgnoringStepsFailure(String message) {
        super(message);
    }

    public IgnoringStepsFailure(String message, Throwable cause) {
        super(message, cause);
    }

}
