package org.jbehave.core.io;

/**
 * <p>
 * Resolves story names from their paths.
 * </p>
 */
public interface StoryNameResolver {

    String resolveName(String path);

}
