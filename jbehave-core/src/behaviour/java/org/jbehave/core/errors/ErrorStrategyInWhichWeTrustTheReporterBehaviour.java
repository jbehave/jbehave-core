package org.jbehave.core.errors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ErrorStrategyInWhichWeTrustTheReporterBehaviour {

    @Test
    public void shouldBreakTheBuildAndTellUsToCheckTheOutput() throws Throwable {
        ErrorStrategy errorStrategy = new ErrorStrategyInWhichWeTrustTheReporter();
        try {
            errorStrategy.handleError(null);
        } catch (AssertionError e) {
            assertThat(e.getMessage(), equalTo("An error occurred while running the stories; please check output for details."));
        }
        
    }
}
