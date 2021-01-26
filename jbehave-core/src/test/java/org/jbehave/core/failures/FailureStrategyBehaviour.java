package org.jbehave.core.failures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FailureStrategyBehaviour {

    @Test
    public void shouldAllowFailuresToBeAbsorbed() {
        new SilentlyAbsorbingFailure().handleFailure(new IllegalStateException());
    }

    @Test
    public void shouldAllowFailuresToBeRethrown() {
        RethrowingFailure rethrowingFailure = new RethrowingFailure();
        IllegalStateException throwable = new IllegalStateException();
        assertThrows(IllegalStateException.class, () -> rethrowingFailure.handleFailure(throwable));
    }

    @Test
    public void shouldAllowFailuresToBeRethrownWhenWrappedAsUUIDExceptions() {
        RethrowingFailure rethrowingFailure = new RethrowingFailure();
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(new IllegalStateException());
        assertThrows(IllegalStateException.class, () -> rethrowingFailure.handleFailure(throwable));
    }

}
