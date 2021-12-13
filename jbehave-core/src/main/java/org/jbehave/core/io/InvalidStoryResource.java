package org.jbehave.core.io;

/**
 * Thrown when a story resource is not valid
 */
@SuppressWarnings("serial")
public class InvalidStoryResource extends RuntimeException {

    public InvalidStoryResource(String storyPath, Throwable cause) {
        super("Invalid story resource for " + storyPath, cause);
    }

}
