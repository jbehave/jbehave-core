package org.jbehave.core.errors;

/**
 * Thrown when a story is not found by the class loader
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class StoryNotFoundException extends RuntimeException {

    public StoryNotFoundException(String message) {
        super(message);
    }

}
