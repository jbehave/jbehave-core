package org.jbehave.scenario.errors;

public class ErrorStrategyInWhichWeTrustTheReporter implements ErrorStrategy {
    public void handleError(Throwable throwable) throws Throwable {
        throw new AssertionError("An error occurred while running the scenarios; please check output for details.");
    }
}
