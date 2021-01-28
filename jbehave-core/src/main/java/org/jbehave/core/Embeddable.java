package org.jbehave.core;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;

/**
 * <p>
 * Represents a runnable facade to the {@link Embedder}. 
 * </p>
 * <p>
 * Users can either extend the abstract implementation {@link ConfigurableEmbedder},
 * which does not implement the {@link #run()} method, or other
 * implementations, such as {@link JUnitStory} or {@link JUnitStories}, which
 * implement {@link #run()} using JUnit's {@link org.junit.Test} annotation.
 * </p>
 * <p>
 * Other test frameworks can be supported in much the same way, by extending the
 * abstract implementation and implementing {@link #run()}.
 * </p>
 * 
 * @see ConfigurableEmbedder
 * @see InjectableEmbedder
 * @see JUnitStory
 * @see JUnitStories
 */
public interface Embeddable {

	void useEmbedder(Embedder embedder);

    void run();

}
