package org.jbehave.core.steps;

import java.util.UUID;

public class CorrelatedException extends RuntimeException {

    private UUID failureCorrelation = UUID.randomUUID();

    public CorrelatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorrelatedException(Throwable cause) {
        super(cause);
    }

    public CorrelatedException(String s) {
        super(s);
    }

    public CorrelatedException() {
    }

    public UUID getUUID() {
        return failureCorrelation;
    }

}
