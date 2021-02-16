package org.jbehave.core.junit;

import org.junit.Test;

import java.util.List;

/**
 * <p>
 * JUnit-runnable entry-point to map stories specified by {@link #storyPaths()},
 * using the meta filters specified by {@link #metaFilters()}.
 * </p>
 */
public abstract class JUnitStoryMaps extends JupiterStories {

    @Override
    @Test
    public void run() {
        configuredEmbedder().mapStoriesAsPaths(storyPaths());
    }

    public abstract List<String> metaFilters();

}
