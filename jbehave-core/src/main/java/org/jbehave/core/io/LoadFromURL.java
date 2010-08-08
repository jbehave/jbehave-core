package org.jbehave.core.io;

import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * Loads story content from URLs
 */
public class LoadFromURL implements StoryLoader {
    public String loadStoryAsText(String storyPath) {
        try {
            URL url = new URL(storyPath);
            return IOUtils.toString(url.openStream());
        } catch (Exception e) {
            throw new InvalidStoryResource(storyPath, e);
        }
    }

}