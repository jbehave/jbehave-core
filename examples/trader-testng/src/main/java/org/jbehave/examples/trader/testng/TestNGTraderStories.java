package org.jbehave.examples.trader.testng;

import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.examples.trader.TraderStories;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

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

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../trader/src/main/java"), "**/*.story", "");
    }

}
