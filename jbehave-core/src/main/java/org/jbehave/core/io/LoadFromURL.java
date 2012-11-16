package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * Loads story resources from URL
 */
public class LoadFromURL implements ResourceLoader, StoryLoader {

    public String loadResourceAsText(String resourcePath) {
        try {
            final InputStream stream = resourceAsStream(resourcePath);
            final String result = IOUtils.toString(stream);
            stream.close();
            return result;
        } catch (Exception cause) {
            throw new InvalidStoryResource(resourcePath, cause);
        }
    }

    public String loadStoryAsText(String storyPath) {
        return loadResourceAsText(storyPath);
    }

    protected InputStream resourceAsStream(String resourcePath) throws IOException {
        return new URL(resourcePath).openStream();
    }

}