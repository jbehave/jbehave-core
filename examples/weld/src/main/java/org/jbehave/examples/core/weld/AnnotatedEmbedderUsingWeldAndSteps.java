package org.jbehave.examples.core.weld;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.weld.WeldAnnotatedEmbedderRunner;
import org.jbehave.examples.core.steps.*;
import org.junit.runner.RunWith;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Run stories via annotated embedder configuration and steps using CDI. The
 * textual core stories are exactly the same ones found in the
 * jbehave-core-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(WeldAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingSteps(instances = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class })
@UsingWeld
public class AnnotatedEmbedderUsingWeldAndSteps extends InjectableEmbedder {

    @Override
    @org.junit.Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
