package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.spring.SpringAnnotatedEmbedder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Run stories via Embedder using JBehave's annotated configuration and steps
 * using Spring
 */
@Configure()
@RunWith(SpringAnnotatedEmbedder.class)
@UsingEmbedder(embedder = Embedder.class, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingSpring(locations = { "org/jbehave/examples/trader/spring/configuration.xml",
        "org/jbehave/examples/trader/spring/steps.xml" })
public class AnnotatedEmbedderUsingSpring extends InjectableEmbedder {

    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(),
                asList("**/stories/*.story"), asList("")));
    }

}
