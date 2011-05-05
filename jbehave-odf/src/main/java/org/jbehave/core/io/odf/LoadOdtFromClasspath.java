package org.jbehave.core.io.odf;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;

import static org.jbehave.core.io.odf.OdfUtils.loadOdt;
import static org.jbehave.core.io.odf.OdfUtils.parseOdt;

/**
 * Loads ODT story resources from classpath
 */
public class LoadOdtFromClasspath extends LoadFromClasspath {

    public LoadOdtFromClasspath(Class<?> loadFromClass) {
        this(loadFromClass.getClassLoader());
    }

    public LoadOdtFromClasspath(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String loadResourceAsText(String storyPath) {
        try {
            return parseOdt(loadOdt(resourceAsStream(storyPath)));
        } catch (Exception cause) {
            throw new InvalidStoryResource(storyPath, cause);
        }
    }

}
