package org.jbehave.examples.trader;

import org.jbehave.core.Embeddable;

/**
 * <p>
 * Example of how multiple stories can be run via TestNG.
 * </p>
 * <p>
 * It uses the same configuration as the TraderStories, except that the
 * {@link Embeddable#run()} method is annotated by the TestNG
 * {@link org.testng.annotations.Test} annotation.
 * </p>
 */
public class TestNGTraderStories extends TraderStories {

    @org.testng.annotations.Test
    public void run() throws Throwable {
        super.run();
    }

}
