package org.jbehave.core.parser;

import org.jbehave.core.RunnableStory;

/**
 * <p>
 * Resolves story paths converting the Java {@link RunnableStory} class to a resource
 * path.
 * </p>
 */
public interface StoryPathResolver {

    String resolve(Class<? extends RunnableStory> storyClass);

}
