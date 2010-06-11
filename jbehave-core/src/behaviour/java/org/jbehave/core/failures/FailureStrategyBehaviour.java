package org.jbehave.core.failures;

import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.junit.Test;

public class FailureStrategyBehaviour {

    @Test
    public void shouldAllowFailuresToBeAbsorbed() throws Throwable {
        new SilentlyAbsorbingFailure().handleFailure(new IllegalStateException());
    }

    @Test(expected=IllegalStateException.class)
    public void shouldAllowFailuresToBeRethrown() throws Throwable {
        new RethrowingFailure().handleFailure(new IllegalStateException());
    }
    
}
