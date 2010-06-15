package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.StoryLocation.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathFinder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.ClasspathTraderEmbedder;
import org.jbehave.examples.trader.TraderSteps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Run stories via Spring JUnit 4 runner
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/org/jbehave/examples/trader/spring/steps.xml"})
public class SpringTraderRunner {

    @Autowired
    private TraderSteps traderSteps;

    @Autowired
    private BeforeAfterSteps beforeAndAfterSteps;

    @Test
    public void runAsJUnit() {
        Embedder embedder = new ClasspathTraderEmbedder() {
            @Override
            public List<CandidateSteps> candidateSteps() {
                return new InstanceStepsFactory(configuration(), traderSteps, beforeAndAfterSteps).createCandidateSteps();
            }
        };
        embedder.runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        String basedir = codeLocationFromClass(this.getClass()).getFile();
        return finder.listStoryPaths(basedir, "", asList("**/*.story"), asList(""));
    }


}