package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Loads story content from classpath resources.
 */
public class LoadFromClasspath implements StoryLoader {

    private final ClassLoader classLoader;

    public LoadFromClasspath() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public LoadFromClasspath(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String loadStoryAsText(String storyPath) {
        InputStream stream = classLoader.getResourceAsStream(storyPath);
        if (stream == null) {
            throw new StoryResourceNotFound(storyPath, classLoader);
        }
        try {
            return IOUtils.toString(stream);
        } catch (IOException e) {
            throw new InvalidStoryResource(storyPath, stream, e);
        }
    }


}