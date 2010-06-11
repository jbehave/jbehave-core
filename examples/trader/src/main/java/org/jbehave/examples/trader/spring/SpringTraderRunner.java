package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;

import java.util.List;

import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.io.StoryPathFinder;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.StepsFactory;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.ClasspathTraderEmbedder;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.converters.TraderConverter;
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

                StoryConfiguration stepsConfiguration = new MostUsefulStoryConfiguration();
                StepMonitor monitor = new SilentStepMonitor();
                stepsConfiguration.useParameterConverters(new ParameterConverters(
                        monitor, new TraderConverter(mockTradePersister())));  // define converter for custom type Trader
                stepsConfiguration.useStepPatternParser(new RegexPrefixCapturingPatternParser("%")); // use '%' instead of '$' to identify parameters
                stepsConfiguration.useStepMonitor(monitor);

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