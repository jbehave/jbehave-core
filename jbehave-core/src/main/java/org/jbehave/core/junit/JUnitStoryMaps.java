package org.jbehave.core.junit;

import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.junit.Test;

/**
 * <p>
 * JUnit-runnable entry-point to map stories specified by {@link #storyPaths()},
 * using the meta filters specified by {@link #metaFilters()}.
 * </p>
 */
public abstract class JUnitStoryMaps extends ConfigurableEmbedder {

    @Test
    public void run() throws Throwable {
        configuredEmbedder().mapStoriesAsPaths(storyPaths());
    }

    protected abstract List<String> metaFilters();

    protected abstract List<String> storyPaths();

}