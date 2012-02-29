package org.jbehave.core.io;

/**
 * {@link PathCalculator} that returns the path provided
 */
public class AbsolutePathCalculator implements PathCalculator {
    public String calculate(String root, String path) {
        return path;
    }
}
