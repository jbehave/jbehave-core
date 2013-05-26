package org.jbehave.examples.core.urls;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.examples.core.CoreEmbedder;

/**
 * Specifies the Embedder for the core example, using URL story loading. It
 * extends CoreEmbedder simply for convenience, in order to avoid
 * duplicating common configuration.
 */
public class URLCoreEmbedder extends CoreEmbedder {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryLoader(new LoadFromURL());
    }

}