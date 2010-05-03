package org.jbehave.core.errors;

/**
 * Thrown when a runnable story does not provide to the story runner
 * all the required parameters.
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class InvalidRunnableStoryException extends RuntimeException {

    public InvalidRunnableStoryException(String message) {
        super(message);
    }

}