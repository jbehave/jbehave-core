package org.jbehave.examples.trader.urls;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.examples.trader.TraderEmbedder;

/**
 * Specifies the Embedder for the Trader example, using URL story loading. It
 * extends TraderEmbedder simply for convenience, in order to avoid
 * duplicating common configuration.
 */
public class URLTraderEmbedder extends TraderEmbedder {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryLoader(new LoadFromURL());
    }

}