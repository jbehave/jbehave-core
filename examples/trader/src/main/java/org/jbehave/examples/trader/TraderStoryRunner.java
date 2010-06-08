package org.jbehave.examples.trader;

import org.junit.Test;

/**
 * Example of how to use one or more StoryEmbedders to embed the story running
 * into any running environment, using any running framework. In this example we
 * are running via JUnit two separate methods. It can be run into an IDE or
 * command-line.
 */
public class TraderStoryRunner {

	@Test
	public void runClasspathLoadedStoriesAsJUnit() {
		// Embedder defines the configuration and candidate steps
		ClasspathTraderEmbedder embedder = new ClasspathTraderEmbedder();
		embedder.embedderConfiguration().doIgnoreFailureInStories(true);
		embedder.runStoriesAsPaths(embedder.storyPaths());
	}

	@Test
	public void runURLLoadedStoriesAsJUnit() {
		// Embedder defines the configuration and candidate steps
		URLTraderEmbedder embedder = new URLTraderEmbedder();
		embedder.runStoriesAsPaths(embedder.storyPaths());
	}

}
