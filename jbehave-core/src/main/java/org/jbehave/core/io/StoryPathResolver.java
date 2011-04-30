package org.jbehave.core.io;

import org.jbehave.core.Embeddable;

/**
 * <p>
 * Resolves story paths converting the Java {@link Embeddable} class to a resource
 * path.
 * </p>
 */
public interface StoryPathResolver {

    String resolve(Class<? extends Embeddable> embeddableClass);

}
