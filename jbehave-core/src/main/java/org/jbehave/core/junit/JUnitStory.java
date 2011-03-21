package org.jbehave.core.junit;

import static java.util.Arrays.asList;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathResolver;
import org.junit.Test;

import java.util.List;

/**
 * <p>
 * JUnit-runnable entry-point to run a single story specified by a {@link Embeddable} class.
 * </p>
 */
public abstract class JUnitStory extends ConfigurableEmbedder {
    
	@Test
    public void run() throws Throwable {        
        Embedder embedder = configuredEmbedder();
        StoryPathResolver pathResolver = embedder.configuration().storyPathResolver();
        String storyPath = pathResolver.resolve(this.getClass());
        try {
            runStoriesAsPaths(embedder, asList(storyPath));
        } finally {
            embedder.generateCrossReference();
        }
    }

    public void runStoriesAsPaths(Embedder embedder, List<String> storyPaths) {
        embedder.runStoriesAsPaths(storyPaths);
    }


}
