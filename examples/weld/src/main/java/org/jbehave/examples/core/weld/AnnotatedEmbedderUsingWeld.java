package org.jbehave.examples.core.weld;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.weld.WeldAnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Run stories via annotated embedder configuration and steps using Weld. The
 * textual core stories are exactly the same ones found in the
 * jbehave-core-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(WeldAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingWeld
public class AnnotatedEmbedderUsingWeld extends InjectableEmbedder {

    @Override
    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }
}
