package org.jbehave.examples.core.needle;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.examples.core.needle.steps.NeedleTraderSteps;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;

/**
 * Here we show how configuration annotations can be split across parent-child hierarchies
 */
@UsingSteps(instances = { NeedleTraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class,
    PendingSteps.class, PriorityMatchingSteps.class, SandpitSteps.class })
public class InheritingAnnotatedEmbedderUsingSteps extends ParentAnnotatedEmbedderUsingNeedle {

    @Override
    @org.junit.Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../trader/src/main/java"), "**/*.story", "");
    }

}
