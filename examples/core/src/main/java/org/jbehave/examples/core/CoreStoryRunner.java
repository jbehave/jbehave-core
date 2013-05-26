package org.jbehave.examples.core;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

/**
 * Example of how to use one or more Embedders to embed the story running into
 * any running environment, using any running framework. In this example we are
 * running via JUnit two separate methods. It can be run into an IDE or
 * command-line.
 */
public class CoreStoryRunner {

    @Test
    public void mapStories() {
        Embedder embedder = new Embedder();
        embedder.useMetaFilters(asList("+author *", "+theme *", "-skip"));
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.mapStoriesAsPaths(storyPaths);
    }

    @Test
    public void runClasspathLoadedStoriesAsJUnit() {
        // CoreEmbedder defines the configuration and steps factory
        Embedder embedder = new CoreEmbedder();
        embedder.embedderControls().doIgnoreFailureInStories(true);
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.runStoriesAsPaths(storyPaths);
    }

}
