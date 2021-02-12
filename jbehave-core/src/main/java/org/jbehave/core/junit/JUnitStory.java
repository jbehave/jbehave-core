package org.jbehave.core.junit;

import java.util.List;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathResolver;
import org.junit.Test;

import static java.util.Arrays.asList;

/**
 * <p>
 * JUnit-runnable entry-point to run a single story specified by an {@link Embeddable} instance,
 * using the configured {@link StoryPathResolver} to resolve the story path from the instance class name.
 * </p>
 */
public abstract class JUnitStory extends JUnitStories {

    @Override
    public List<String> storyPaths() {
        StoryPathResolver pathResolver = configuredEmbedder().configuration().storyPathResolver();
        String storyPath = pathResolver.resolve(this.getClass());
        return asList(storyPath);
    }

}
