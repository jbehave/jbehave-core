package org.jbehave.core.failures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FailureStrategyBehaviour {

    @Test
    void shouldAllowFailuresToBeAbsorbed() {
        new SilentlyAbsorbingFailure().handleFailure(new IllegalStateException());
    }

    @Test
    void shouldAllowFailuresToBeRethrown() {
        RethrowingFailure rethrowingFailure = new RethrowingFailure();
        IllegalStateException throwable = new IllegalStateException();
        assertThrows(IllegalStateException.class, () -> rethrowingFailure.handleFailure(throwable));
    }

    @Test
    void shouldAllowFailuresToBeRethrownWhenWrappedAsUUIDExceptions() {
        RethrowingFailure rethrowingFailure = new RethrowingFailure();
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(new IllegalStateException());
        assertThrows(IllegalStateException.class, () -> rethrowingFailure.handleFailure(throwable));
    }

}
