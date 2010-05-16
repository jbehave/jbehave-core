package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.JUnitStories;
import org.jbehave.core.StoryEmbedder;

/**
 * Example of how multiple stories can be run via JUnit using 
 * a StoryEmbedder to specify the configuration and candidate steps.
 */
public class TraderStories extends JUnitStories {

	private URLTraderStoryEmbedder embedder = new URLTraderStoryEmbedder();

	@Override
	protected StoryEmbedder configuredEmbedder() {
		return embedder;
	}

	@Override
	protected List<String> storyPaths() {
		return embedder.storyPaths();
	}
        
}