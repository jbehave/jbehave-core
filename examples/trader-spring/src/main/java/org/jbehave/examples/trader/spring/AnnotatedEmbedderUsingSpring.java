package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.spring.SpringAnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Run stories via annotated embedder configuration and steps using Spring. The
 * textual trader stories are exactly the same ones found in the
 * jbehave-trader-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(SpringAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingSpring(resources = { "org/jbehave/examples/trader/spring/configuration.xml",
        "org/jbehave/examples/trader/spring/steps.xml" })
public class AnnotatedEmbedderUsingSpring extends InjectableEmbedder {

    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        String searchInDirectory = codeLocationFromPath("../trader/src/main/java").getFile();
        return new StoryFinder().findPaths(searchInDirectory, asList("**/*.story"), null);
    }

}
