package org.jbehave.core.junit;

import org.junit.Test;

/**
 * <p>
 * JUnit-runnable entry-point to run multiple stories specified by {@link #storyPaths()}.
 * The {@link #run()} method is annotated as JUnit {@link Test}.
 * </p>
 */
public abstract class JUnitStories extends JupiterStories {

    @Override
    @Test
    public void run() {
        super.run();
    }

}
