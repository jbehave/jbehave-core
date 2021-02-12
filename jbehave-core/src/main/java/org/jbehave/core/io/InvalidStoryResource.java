package org.jbehave.core.io;

import java.io.InputStream;

/**
 * Thrown when a story resource is not valid
 */
@SuppressWarnings("serial")
public class InvalidStoryResource extends RuntimeException {

    public InvalidStoryResource(String storyPath, Throwable cause) {
        super("Invalid story resource for " + storyPath, cause);
    }

    public InvalidStoryResource(String storyPath, InputStream stream,
            Throwable cause) {
        super("Invalid story resource for " + storyPath + " from input stream " + stream, cause);
    }

}
