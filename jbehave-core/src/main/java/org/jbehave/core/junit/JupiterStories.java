package org.jbehave.core.junit;

import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Jupiter-runnable entry-point to run multiple stories specified by {@link #storyPaths()}.
 * The {@link #run()} method is annotated as Jupiter {@link Test}.
 * </p>
 */
public abstract class JupiterStories extends ConfigurableEmbedder {

    @Override
    @Test
    public void run() {
        Embedder embedder = configuredEmbedder();
        try {
            embedder.runStoriesAsPaths(storyPaths());
        } finally {
            embedder.generateSurefireReport();
        }
    }

    public abstract List<String> storyPaths();

}
