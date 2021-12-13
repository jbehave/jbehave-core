package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

/**
 * Loads story resources from URL
 */
public class LoadFromURL implements ResourceLoader, StoryLoader {

    private final Charset charset;

    public LoadFromURL() {
        this(StandardCharsets.UTF_8);
    }

    public LoadFromURL(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String loadResourceAsText(String resourcePath) {
        try (InputStream resourceAsStream = resourceAsStream(resourcePath)) {
            return IOUtils.toString(resourceAsStream, charset);
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
