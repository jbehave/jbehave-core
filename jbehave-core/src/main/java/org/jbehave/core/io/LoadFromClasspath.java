package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Loads story resources from classpath
 */
public class LoadFromClasspath implements StoryLoader {

    private final Charset charset;

    protected final ClassLoader classLoader;

    /**
     * Uses default encoding UTF-8.
     * @see {@link #LoadFromClasspath(Charset)}.
     */
    public LoadFromClasspath() {
        this(StandardCharsets.UTF_8);
    }
    
    /**
     * Uses encoding provided.
     * @param charset the Charset
     * @see {@link #LoadFromClasspath(ClassLoader,Charset)}.
     */
    public LoadFromClasspath(Charset charset) {
        this(Thread.currentThread().getContextClassLoader(), charset);
    }

    /**
     * Uses a class to get the ClassLoader
     * @param loadFromClass the Class to get the ClassLoader from
     * @see {@link #LoadFromClasspath(ClassLoader)}.
     */
    public LoadFromClasspath(Class<?> loadFromClass) {
        this(loadFromClass.getClassLoader());
    }

    /**
     * Uses default encoding UTF-8
     * @param classLoader the ClassLoader
     */
    public LoadFromClasspath(ClassLoader classLoader) {
        this(classLoader, StandardCharsets.UTF_8);
    }
    
    /**
     * Uses classloader and encoding provided.
     * @param classLoader the ClassLoader
     * @param charset the Charset
     */
    public LoadFromClasspath(ClassLoader classLoader, Charset charset) {
        this.classLoader = classLoader;
        this.charset = charset;
    }

    public String loadResourceAsText(String resourcePath) {
        InputStream stream = resourceAsStream(resourcePath);
        try {
            return IOUtils.toString(stream, charset, true);
        } catch (IOException e) {
            throw new InvalidStoryResource(resourcePath, stream, e);
        }
    }

    public String loadStoryAsText(String storyPath) {
        return loadResourceAsText(storyPath);
    }

    protected InputStream resourceAsStream(String resourcePath) {
        InputStream stream = classLoader.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new StoryResourceNotFound(resourcePath, classLoader);
        }
        return stream;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
