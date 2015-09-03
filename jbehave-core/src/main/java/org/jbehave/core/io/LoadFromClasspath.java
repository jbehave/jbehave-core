package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Loads story resources from classpath
 */
public class LoadFromClasspath implements StoryLoader {
	
	private final Charset charset;

    protected final ClassLoader classLoader;

    /**
     * Uses default enconding UTF-8.
     */
    public LoadFromClasspath() {
        this(Charsets.UTF_8);
    }
    
    public LoadFromClasspath(Charset charset) {
    	this(Thread.currentThread().getContextClassLoader(), charset);
    }

    /**
     * @see {@link #LoadFromClasspath(ClassLoader)}.
     */
    public LoadFromClasspath(Class<?> loadFromClass) {
        this(loadFromClass.getClassLoader());
    }

    /**
     * Uses default enconding UTF-8
     */
    public LoadFromClasspath(ClassLoader classLoader) {
        this(classLoader, Charsets.UTF_8);
    }
    
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