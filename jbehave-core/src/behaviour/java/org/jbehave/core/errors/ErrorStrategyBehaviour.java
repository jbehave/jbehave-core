package org.jbehave.core.errors;

import org.junit.Test;

public class ErrorStrategyBehaviour {

    @Test(expected=IllegalStateException.class)
    public void shouldAllowErrorsToBeRethrown() throws Throwable {
        ErrorStrategy.RETHROW.handleError(new IllegalStateException());
    }
    
    @Test
    public void shouldAllowErrorsToBeSwallowed() throws Throwable {
        ErrorStrategy.SILENT.handleError(new IllegalStateException());
    }
}
