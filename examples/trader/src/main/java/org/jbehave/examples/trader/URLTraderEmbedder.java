package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.StoryFinder;

/**
 * Specifies the Embedder for the Trader example, using URL story loading. It
 * extends ClasspathTraderEmbedder simply for convenience, in order to avoid
 * duplicating common configuration.
 */
public class URLTraderEmbedder extends ClasspathTraderEmbedder {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryLoader(new LoadFromURL());
    }

    @Override
    public List<String> storyPaths() {
        String codeLocation = codeLocationFromClass(this.getClass()).getFile();
        return new StoryFinder().findPaths(codeLocation, asList("**/trader_is_alerted_of_status.story",
                "**/traders_can_be_subset.story"), asList(""), "file:" + codeLocation);
    }

}