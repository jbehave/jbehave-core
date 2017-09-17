package org.jbehave.examples.core.urls;

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
    public void runURLLoadedStoriesAsJUnit() {
        // Embedder defines the configuration and candidate steps
        Embedder embedder = new URLCoreEmbedder();
        String codeLocation = codeLocationFromClass(this.getClass()).getFile();
        List<String> storyPaths = new StoryFinder().findPaths(codeLocation, asList(
                "**/trader_is_alerted_of_status.story", "**/traders_can_be_searched.story"), null, "file:"
                + codeLocation);
        embedder.runStoriesAsPaths(storyPaths);
    }

}
