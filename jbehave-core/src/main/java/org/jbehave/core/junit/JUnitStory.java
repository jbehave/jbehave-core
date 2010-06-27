package org.jbehave.core.junit;

import static java.util.Arrays.asList;

import org.jbehave.core.AbstractStory;
import org.jbehave.core.RunnableStory;
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
        Class<? extends RunnableStory> storyClass = this.getClass();
        configuredEmbedder().runStoriesAsClasses(asList(storyClass));
    }

 
}
