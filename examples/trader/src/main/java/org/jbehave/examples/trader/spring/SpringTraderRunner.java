package org.jbehave.examples.trader.spring;

import org.jbehave.core.StoryEmbedder;
import org.jbehave.core.parser.PrefixCapturingPatternBuilder;
import org.jbehave.core.parser.StoryLocation;
import org.jbehave.core.parser.StoryPathFinder;
import org.jbehave.core.steps.*;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.ClasspathTraderStoryEmbedder;
import org.jbehave.examples.trader.converters.TraderConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.Arrays.asList;

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
        StoryEmbedder embedder = new ClasspathTraderStoryEmbedder() {
            @Override
            public List<CandidateSteps> candidateSteps() {

                StepsConfiguration stepsConfiguration = new MostUsefulStepsConfiguration();
                StepMonitor monitor = new SilentStepMonitor();
                stepsConfiguration.useParameterConverters(new ParameterConverters(
                        monitor, new TraderConverter(mockTradePersister())));  // define converter for custom type Trader
                stepsConfiguration.usePatternBuilder(new PrefixCapturingPatternBuilder("%")); // use '%' instead of '$' to identify parameters
                stepsConfiguration.useMonitor(monitor);

                return asList(new StepsFactory(stepsConfiguration).createCandidateSteps(traderSteps, beforeAndAfterSteps));
            }
        };
        embedder.runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        String basedir = new StoryLocation("", this.getClass()).getCodeLocation().getFile();
        return finder.listStoryPaths(basedir, "", asList("**/*.story"), asList(""));
    }


}