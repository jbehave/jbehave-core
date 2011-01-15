package org.jbehave.core.failures;

import java.util.UUID;

/**
 * Add a UUID for an exception (by wrapping it).
 * This allows a unique identifier to be used repeatedly to represent the exception (step failure)
 * throughout the reports.  In particular, it was added to allow failing-scenario screen-shots to
 * be linked to from the HTML report of the web-selenium module of jbehave-web.
 */
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
