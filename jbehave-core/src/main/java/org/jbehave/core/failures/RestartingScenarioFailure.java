package org.jbehave.core.failures;

/**
 * Runtime exception thrown to indicate that the scenario should be restarted.
 */
@SuppressWarnings("serial")
public class RestartingScenarioFailure extends RuntimeException {
    
    public RestartingScenarioFailure(String message) {
        super(message);
    }

    public RestartingScenarioFailure(String message, Throwable cause) {
        super(message, cause);
    }

}
