package org.jbehave.core.junit;

import org.jbehave.core.io.StoryPathResolver;

import java.util.List;

import static java.util.Arrays.asList;

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
