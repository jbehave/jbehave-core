package org.jbehave.core.failures;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class FailureStrategyBehaviour {

    @Test
    public void shouldAllowFailuresToBeAbsorbed() {
        new SilentlyAbsorbingFailure().handleFailure(new IllegalStateException());
    }

    @Test
    public void shouldAllowFailuresToBeRethrown() throws Throwable {
        try {
            new RethrowingFailure().handleFailure(new IllegalStateException());
        } catch (Throwable throwable) {
            assertThat(throwable, is(instanceOf(IllegalStateException.class)));
        }
    }

    @Test
    public void shouldAllowFailuresToBeRethrownWhenWrappedAsUUIDExceptions() throws Throwable {
        try {
            new RethrowingFailure().handleFailure(new UUIDExceptionWrapper(new IllegalStateException()));
        } catch (Throwable throwable) {
            assertThat(throwable, is(instanceOf(IllegalStateException.class)));
        }
    }

}
