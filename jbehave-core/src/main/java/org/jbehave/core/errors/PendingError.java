package org.jbehave.core.errors;

public class PendingError extends AssertionError {

    private static final long serialVersionUID = 9038975723473227215L;

    public PendingError(String description) {
        super("Pending: " + description);
    }

}
