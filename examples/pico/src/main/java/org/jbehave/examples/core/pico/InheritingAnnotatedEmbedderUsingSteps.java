package org.jbehave.examples.core.pico;

import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.examples.core.steps.*;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

@UsingSteps(instances = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class, PendingSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class, SearchSteps.class })
public class InheritingAnnotatedEmbedderUsingSteps extends ParentAnnotatedEmbedderUsingPico {

    @Override
    @org.junit.Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
