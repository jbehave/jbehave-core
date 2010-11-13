package org.jbehave.core.io;

import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * Loads story resources from URLs
 */
public class LoadFromURL implements ResourceLoader, StoryLoader {

    public String loadResourceAsText(String resourcePath) {
        try {
            URL url = new URL(resourcePath);
            return IOUtils.toString(url.openStream());
        } catch (Exception e) {
            throw new InvalidStoryResource(resourcePath, e);
        }
    }

    public String loadStoryAsText(String storyPath) {
        return loadResourceAsText(storyPath);
    }

}