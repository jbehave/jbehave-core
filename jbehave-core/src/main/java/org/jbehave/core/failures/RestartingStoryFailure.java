package org.jbehave.core.failures;

/**
 * Runtime exception thrown to indicate that the story should be restarted.
 */
@SuppressWarnings("serial")
public class RestartingStoryFailure extends RuntimeException {
    
    public RestartingStoryFailure(String message) {
        super(message);
    }

    public RestartingStoryFailure(String message, Throwable cause) {
        super(message, cause);
    }

}
