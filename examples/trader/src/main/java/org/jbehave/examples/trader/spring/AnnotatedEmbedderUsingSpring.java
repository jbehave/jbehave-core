package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;

import org.jbehave.core.annotations.WithConfiguration;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.spring.SpringAnnotationBuilder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathFinder;
import org.junit.Test;

/**
 *  Run stories via Embedder using JBehave's annotated configuration and steps using
 *  a Spring context build from separate locations for configuration and steps.
 */
@WithConfiguration()
@UsingSpring(locations = { "org/jbehave/examples/trader/spring/configuration.xml", "org/jbehave/examples/trader/spring/steps.xml" })
public class AnnotatedEmbedderUsingSpring {

    @Test
    public void run() {
        Embedder embedder = new Embedder();
        embedder.useConfiguration(new SpringAnnotationBuilder().buildConfiguration(this));
        embedder.useCandidateSteps(new SpringAnnotationBuilder().buildCandidateSteps(this));
        embedder.embedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
        embedder.runStoriesAsPaths(new StoryPathFinder().listStoryPaths("target/classes", "",
                asList("**/stories/*.story"), asList("")));
    }

}
