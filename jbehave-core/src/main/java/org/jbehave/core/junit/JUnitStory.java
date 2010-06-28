package org.jbehave.core.junit;

import static java.util.Arrays.asList;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.junit.Test;

/**
 * <p>
 * JUnit-runnable entry-point to run a single story specified by a {@link Embeddable} class.
 * </p>
 */
public abstract class JUnitStory extends ConfigurableEmbedder {
    
    @SuppressWarnings("unchecked")
	@Test
    public void run() throws Throwable {
        configuredEmbedder().runStoriesAsClasses(asList((Class<? extends Embeddable>) this.getClass()));
    }

 
}
