package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;

/**
 * Example of how multiple stories can be run via JUnit using 
 * a Embedder to specify the configuration and candidate steps.
 */
public class TraderStories extends JUnitStories {

	private URLTraderEmbedder embedder = new URLTraderEmbedder();

	@Override
	protected Embedder configuredEmbedder() {
		return embedder;
	}

	@Override
	protected List<String> storyPaths() {
		return embedder.storyPaths();
	}
        
}