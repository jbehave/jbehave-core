package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Loads story resources from URL
 */
public class LoadFromURL implements ResourceLoader, StoryLoader {

    @Override
    public String loadResourceAsText(String resourcePath) {
        try {
            return IOUtils.toString(resourceAsStream(resourcePath), true);
        } catch (Exception cause) {
            throw new InvalidStoryResource(resourcePath, cause);
        }
    }

    @Override
    public String loadStoryAsText(String storyPath) {
        return loadResourceAsText(storyPath);
    }

    protected InputStream resourceAsStream(String resourcePath) throws IOException {
        return new URL(resourcePath).openStream();
    }

}
