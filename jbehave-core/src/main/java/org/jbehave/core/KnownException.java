package org.jbehave.core;

public class KnownException extends RuntimeException {

    public KnownException() {
    }

    public KnownException(String s) {
        super(s);
    }

    public KnownException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public KnownException(Throwable throwable) {
        super(throwable);
    }
}
