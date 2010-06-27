package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.spring.SpringAnnotationBuilder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.junit.Test;

/**
 *  Run stories via Embedder using JBehave's annotated configuration using
 *  a Spring context built from separate locations for configuration and steps.
 */
@Configure()
@UsingSpring(locations = { "org/jbehave/examples/trader/spring/configuration.xml", "org/jbehave/examples/trader/spring/steps.xml" })
public class AnnotatedEmbedderUsingSpring {

    @Test
    public void run() {
        Embedder embedder = new Embedder();
        SpringAnnotationBuilder builder = new SpringAnnotationBuilder(this.getClass());
        embedder.useConfiguration(builder.buildConfiguration());
        embedder.useCandidateSteps(builder.buildCandidateSteps());
        embedder.embedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
        embedder.runStoriesAsPaths(new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/stories/*.story"),
                asList("")));
    }

}
