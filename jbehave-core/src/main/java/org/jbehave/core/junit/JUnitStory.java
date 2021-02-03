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
 * JUnit-runnable entry-point to run a single story specified by a {@link Embeddable} class.
 * </p>
 */
public abstract class JUnitStory extends JUnitStories {

    @Override
    protected List<String> storyPaths() {
        StoryPathResolver pathResolver = configuredEmbedder().configuration().storyPathResolver();
        String storyPath = pathResolver.resolve(this.getClass());
        return asList(storyPath);
    }

}
