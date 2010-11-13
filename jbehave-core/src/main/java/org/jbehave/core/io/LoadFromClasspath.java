package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Loads story resources from classpath.
 */
public class LoadFromClasspath implements ResourceLoader, StoryLoader {

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

    public String loadResourceAsText(String resourcePath) {
        InputStream stream = classLoader.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new StoryResourceNotFound(resourcePath, classLoader);
        }
        try {
            return IOUtils.toString(stream);
        } catch (IOException e) {
            throw new InvalidStoryResource(resourcePath, stream, e);
        }
    }

    public String loadStoryAsText(String storyPath) {
        return loadResourceAsText(storyPath);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}