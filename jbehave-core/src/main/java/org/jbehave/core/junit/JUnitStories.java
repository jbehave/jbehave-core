package org.jbehave.core.junit;

import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.junit.Test;

/**
 * <p>
 * JUnit-runnable entry-point to run multiple stories specified by {@link JUnitStories#storyPaths()}.
 * </p>
 */
public abstract class JUnitStories extends ConfigurableEmbedder {

    @Test
    public void run() throws Throwable {
        Embedder embedder = configuredEmbedder();
        try {
            embedder.runStoriesAsPaths(storyPaths());
        } finally {
            embedder.generateCrossReference();
        }
    }

    protected abstract List<String> storyPaths();

}