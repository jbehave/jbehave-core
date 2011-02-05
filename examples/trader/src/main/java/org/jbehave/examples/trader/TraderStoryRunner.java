package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.junit.Test;

/**
 * Example of how to use one or more Embedders to embed the story running into
 * any running environment, using any running framework. In this example we are
 * running via JUnit two separate methods. It can be run into an IDE or
 * command-line.
 */
public class TraderStoryRunner {

    @Test
    public void mapStories() {
        Embedder embedder = new Embedder();
        embedder.useMetaFilters(asList("+author *", "+theme *", "-skip"));
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.mapStoriesAsPaths(storyPaths);
    }

    @Test
    public void navigateStories() {
        TraderEmbedder embedder = new TraderEmbedder();
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.runStoriesAsPaths(storyPaths);
        embedder.generateNavigatorView(embedder.getCrossReference());
    }

    @Test
    public void runClasspathLoadedStoriesAsJUnit() {
        // Embedder defines the configuration and candidate steps
        Embedder embedder = new TraderEmbedder();
        embedder.embedderControls().doIgnoreFailureInStories(true);
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.runStoriesAsPaths(storyPaths);
    }

    @Test
    public void runURLLoadedStoriesAsJUnit() {
        // Embedder defines the configuration and candidate steps
        Embedder embedder = new URLTraderEmbedder();
        String codeLocation = codeLocationFromClass(this.getClass()).getFile();
        List<String> storyPaths = new StoryFinder().findPaths(codeLocation, asList(
                "**/trader_is_alerted_of_status.story", "**/traders_can_be_subset.story"), null, "file:"
                + codeLocation);
        embedder.runStoriesAsPaths(storyPaths);
    }

}
