package org.jbehave.core.errors;

/**
 * Thrown when a story resource is not valid
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class InvalidStoryResourceException extends RuntimeException {

    public InvalidStoryResourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
