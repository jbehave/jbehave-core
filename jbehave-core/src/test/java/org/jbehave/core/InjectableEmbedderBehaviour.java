package org.jbehave.core;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jbehave.core.embedder.Embedder;
import org.junit.jupiter.api.Test;

class InjectableEmbedderBehaviour {

	@Test
    void shouldRunStoriesAsPathsUsingInjected() {
        // Given
        Embedder embedder = mock(Embedder.class);

        // When
        StoriesAsPaths stories = new StoriesAsPaths();
        stories.useEmbedder(embedder);
        stories.run();

        // Then
        verify(embedder).runStoriesAsPaths(asList("org/jbehave/core/story1", "org/jbehave/core/story2"));
    }

    private class StoriesAsPaths extends InjectableEmbedder {

        @Override
        public void run() {
            injectedEmbedder().runStoriesAsPaths(asList("org/jbehave/core/story1", "org/jbehave/core/story2"));
        }

        
    }



}
