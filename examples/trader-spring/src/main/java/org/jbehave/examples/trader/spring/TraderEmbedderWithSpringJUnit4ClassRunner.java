package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.TraderEmbedder;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.TraderSteps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Run stories via Embedder using Spring's {@link SpringJUnit4ClassRunner} to inject the
 * steps instances from a given Spring context.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jbehave/examples/trader/spring/steps.xml" })
public class TraderEmbedderWithSpringJUnit4ClassRunner {

    @Autowired
    private TraderSteps traderSteps;

    @Autowired
    private BeforeAfterSteps beforeAndAfterSteps;

    @Test
    public void runStoriesAsPaths() {
        embedder().runStoriesAsPaths(storyPaths());
    }

    @Test
    public void findMatchingCandidateSteps() {
        embedder().reportMatchingStepdocs("When traders are subset to \".*y\" by name");
        embedder().reportMatchingStepdocs("Given a step that is not matched");
    }

    @Test
    public void findMatchingCandidateStepsWithNoStepsInstancesProvided() {
        new Embedder().reportMatchingStepdocs("Given a step that cannot be matched");
    }

    private Embedder embedder() {
        Embedder embedder = new TraderEmbedder();
        embedder.useCandidateSteps(new InstanceStepsFactory(embedder.configuration(), traderSteps, beforeAndAfterSteps)
                .createCandidateSteps());
        return embedder;
    }

    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/*.story"), asList(""));
    }

}