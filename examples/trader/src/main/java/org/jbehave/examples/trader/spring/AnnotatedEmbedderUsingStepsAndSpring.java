package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.spring.SpringAnnotatedEmbedderRunner;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.stories.AndStep.AndSteps;
import org.jbehave.examples.trader.stories.ClaimsWithNullCalendar.CalendarSteps;
import org.jbehave.examples.trader.stories.FailureFollowedByGivenStories.SandpitSteps;
import org.jbehave.examples.trader.stories.PriorityMatching.PriorityMatchingSteps;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Run stories via Embedder using JBehave's annotated configuration and steps
 * using Spring
 */
@RunWith(SpringAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingSteps(instances = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class })
@UsingSpring(resources = { "org/jbehave/examples/trader/spring/configuration.xml"})
public class AnnotatedEmbedderUsingStepsAndSpring extends InjectableEmbedder {

    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(),
                asList("**/spring/stories/*.story"), asList("")));
    }

}
