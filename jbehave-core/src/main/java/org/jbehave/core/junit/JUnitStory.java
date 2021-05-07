package org.jbehave.core.junit;

import static java.util.Arrays.asList;

import java.util.List;

import org.jbehave.core.io.StoryPathResolver;

/**
 * <p>
 * JUnit-runnable entry-point to run a single story using the configured {@link StoryPathResolver}
 * to resolve the story path from the instance class name.
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
