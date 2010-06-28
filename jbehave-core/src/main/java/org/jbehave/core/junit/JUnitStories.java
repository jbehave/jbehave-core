package org.jbehave.core.junit;

import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.junit.Test;

/**
 * <p>
 * JUnit-runnable entry-point to run multiple stories specified by {@link JUnitStories#storyPaths()}.
 * </p>
 */
public abstract class JUnitStories extends ConfigurableEmbedder {

    @Test
    public void run() throws Throwable {
        configuredEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected abstract List<String> storyPaths();

}