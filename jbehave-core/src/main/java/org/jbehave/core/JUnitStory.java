package org.jbehave.core;

import static java.util.Arrays.asList;

import org.junit.Test;

/**
 * <p>
 * JUnit-runnable entry-point to run a single story specified by a {@link RunnableStory} class.
 * </p>
 */
public abstract class JUnitStory extends AbstractStory {
    
    @SuppressWarnings("unchecked")
	@Test
    public void run() throws Throwable {
        StoryEmbedder embedder = storyEmbedder();
        Class<? extends RunnableStory> storyClass = this.getClass();
        embedder.runStoriesAsClasses(asList(storyClass));
    }

 
}
