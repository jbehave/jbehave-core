package org.jbehave.core.failures;

import org.junit.Test;

public class FailureStrategyBehaviour {

    @Test
    public void shouldAllowFailuresToBeAbsorbed() {
        new SilentlyAbsorbingFailure().handleFailure(new IllegalStateException());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldAllowFailuresToBeRethrown() throws Throwable {
        new RethrowingFailure().handleFailure(new IllegalStateException());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldAllowFailuresToBeRethrownWhenWrappedAsUUIDExceptions() throws Throwable {
        new RethrowingFailure().handleFailure(new UUIDExceptionWrapper(new IllegalStateException()));
    }

}
