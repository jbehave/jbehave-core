package org.jbehave.examples.core.testng;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.examples.core.CoreStories;

/**
 * <p>
 * Example of how multiple stories can be run via TestNG.
 * </p>
 * <p>
 * It uses the same configuration as the CoreStories, except that the
 * {@link Embeddable#run()} method is annotated by the TestNG
 * {@link org.testng.annotations.Test} annotation.
 * </p>
 */
public class CoreStoriesUsingTestNG extends CoreStories {

    @Override
    @org.testng.annotations.Test
    public void run() {
        super.run();
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
