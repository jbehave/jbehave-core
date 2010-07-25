package org.jbehave.core.io;

import java.util.List;

/**
 * Thrown when a story resource is not found
 */
@SuppressWarnings("serial")
public class StoryResourceNotFound extends RuntimeException {

    public StoryResourceNotFound(String storyPath, ClassLoader classLoader) {
        super("Story path '" + storyPath + "' not found by class loader " + classLoader);
    }

    public StoryResourceNotFound(String storyPath, Exception cause) {
        super("Story path '" + storyPath + "' not found: " + cause, cause);
    }

    public StoryResourceNotFound(String storyPath, List<String> traversalPaths) {
        super("Story path '" + storyPath + "' not found: while looking in '" + traversalPaths + "'");
    }

}
