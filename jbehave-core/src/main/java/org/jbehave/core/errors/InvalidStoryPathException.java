package org.jbehave.core.errors;

/**
 * Thrown when the path of a story is not valid
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class InvalidStoryPathException extends RuntimeException {

    public InvalidStoryPathException(String message) {
        super(message);
    }

}
