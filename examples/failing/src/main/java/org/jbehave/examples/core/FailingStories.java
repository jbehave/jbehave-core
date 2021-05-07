package org.jbehave.examples.core;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;

/**
 * <p>
 * Example of how multiple stories can be run via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the
 * {@link LoadFromClasspath} story loader is configured.
 * </p>
 */
public class FailingStories extends CoreStories {

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(CoreStories.class), "**/failing/*.story","**/given_relative_path*");
    }
}
