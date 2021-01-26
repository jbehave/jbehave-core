package org.jbehave.examples.core.weld;

import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.examples.core.steps.*;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Here we show how configuation annotations can be split across parent-child hierarchies
 */
@UsingSteps(instances = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class })
public class InheritingAnnotatedEmbedderUsingSteps extends ParentAnnotatedEmbedderUsingWeld {

    @Override
    @org.junit.Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
