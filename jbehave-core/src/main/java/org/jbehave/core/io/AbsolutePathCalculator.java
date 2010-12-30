package org.jbehave.core.io;

/**
 *
 *
 */
public class AbsolutePathCalculator implements PathCalculator {
    @Override
    public String calculate(String root, String path) {
        return path;
    }
}
