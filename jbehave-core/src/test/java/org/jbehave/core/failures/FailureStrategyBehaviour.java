package org.jbehave.core.failures;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
    void shouldAllowFailuresToBeRethrownWhenWrappedAsUuidExceptions() {
        RethrowingFailure rethrowingFailure = new RethrowingFailure();
        UUIDExceptionWrapper throwable = new UUIDExceptionWrapper(new IllegalStateException());
        assertThrows(IllegalStateException.class, () -> rethrowingFailure.handleFailure(throwable));
    }

}
