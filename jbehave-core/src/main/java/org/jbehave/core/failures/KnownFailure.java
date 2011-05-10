package org.jbehave.core.failures;

/**
 * Runtime exception thrown to indicate the occurrence of a known failure.  
 * A known failure will typically be handled with less verbose reporting.
 */
@SuppressWarnings("serial")
public class KnownFailure extends RuntimeException {

    public KnownFailure() {
    }

    public KnownFailure(String message) {
        super(message);
    }

    public KnownFailure(Throwable cause) {
        super(cause);
    }

    public KnownFailure(String message, Throwable cause) {
        super(message, cause);
    }

}
