package org.jbehave.core;

import org.jbehave.core.embedder.Embedder;

/**
 * <p>
 * Abstract implementation of {@link Embeddable} which allows to inject
 * the {@link Embedder} used to run the story or stories.
 * </p>
 */
public abstract class InjectableEmbedder implements Embeddable {

    private Embedder embedder = new Embedder();

    public void useEmbedder(Embedder embedder) {
        this.embedder = embedder;
    }

    public Embedder injectedEmbedder() {
        return embedder;
    }

}
