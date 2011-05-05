package org.jbehave.core.failures;

import java.util.UUID;

/**
 * Wraps an exception by adding an {@link UUID}. This allows a unique identifier
 * to be used repeatedly to represent the exception throw in a step failure
 * throughout the reports. In particular, it allows failing scenario screenshots
 * to be linked to from the HTML report.
 */
@SuppressWarnings("serial")
public class UUIDExceptionWrapper extends RuntimeException {

    private UUID uuid = UUID.randomUUID();

    public UUIDExceptionWrapper(String message, Throwable cause) {
        super(message, cause);
    }

    public UUIDExceptionWrapper(Throwable cause) {
        super(cause);
    }

    public UUIDExceptionWrapper(String s) {
        super(s);
    }

    public UUIDExceptionWrapper() {
    }

    public UUID getUUID() {
        return uuid;
    }

}
