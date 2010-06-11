package org.jbehave.examples.trader;

import static java.util.Arrays.asList;

import java.text.MessageFormat;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.StoryLocation;

/**
 * Specifies the Embedder for the Trader example, using URL story loading.
 * It extends ClasspathTraderEmbedder simply for convenience, in order to 
 * avoid duplicating common configuration.
 */
public class URLTraderEmbedder extends ClasspathTraderEmbedder {

	@Override
	public Configuration configuration() {
		return super.configuration().useStoryLoader(new LoadFromURL());
	}

	@Override
	public List<String> storyPaths() {
		// Defining story paths via URLs
		return asList(storyURL("trader_is_alerted_of_status.story"),
				storyURL("traders_can_be_subset.story"));
	}

	private String storyURL(String name) {
		String codeLocation = new StoryLocation("", this.getClass())
				.getCodeLocation().getFile();
		String urlPattern = "file:" + codeLocation
				+ "org/jbehave/examples/trader/stories/{0}";
		return MessageFormat.format(urlPattern, name);

	}

}