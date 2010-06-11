package org.jbehave.core.io;

import java.io.InputStream;

/**
 * Thrown when a story resource is not valid
 */
@SuppressWarnings("serial")
public class InvalidStoryResource extends RuntimeException {

    public InvalidStoryResource(String message, Throwable cause) {
        super(message, cause);
    }

	public InvalidStoryResource(String storyPath, InputStream stream,
			Throwable cause) {
		super("Invalid story content for " + storyPath + " from resource stream " + stream, cause);
	}

}
