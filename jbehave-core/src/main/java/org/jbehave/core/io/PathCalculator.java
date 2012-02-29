package org.jbehave.core.io;

/**
 * Calculates the paths to given stories.
 */
public interface PathCalculator {
    String calculate(String root, String path);
}
