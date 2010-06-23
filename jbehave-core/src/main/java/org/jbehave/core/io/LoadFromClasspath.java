package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Loads story content from classpath resources.
 */
public class LoadFromClasspath implements StoryLoader {

    private final ClassLoader classLoader;

    public LoadFromClasspath() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public LoadFromClasspath(Class<?> loadFromClass) {
        this(loadFromClass.getClassLoader());
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}