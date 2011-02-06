package org.jbehave.examples.trader;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.CompositeSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.steps.TraderSteps;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

/**
 * <p>
 * Example of how to run a single story via JUnit. JUnitStory is a simple facade
 * around the Embedder. The user need only provide the configuration and the
 * CandidateSteps. Using this paradigm (which is the analogous to the one used
 * in JBehave 2) each story class must extends this class and maps one-to-one to
 * a textual story via the configured {@link StoryPathResolver}.
 * </p>
 * <p>
 * Users wanting to run multiple stories via the same Java class (new to JBehave
 * 3) should look at {@link TraderStories}, {@link TraderStoryRunner} or
 * {@link TraderAnnotatedEmbedderRunner}
 * </p>
 */
public abstract class TraderStory extends JUnitStory {

    public TraderStory() {
        configuredEmbedder().embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true);
//        Uncomment to set meta filter, which can also be set via Ant or Maven
//        configuredEmbedder().useMetaFilters(Arrays.asList("+theme parametrisation"));
    }

    @Override
    public Configuration configuration() {
        Class<? extends Embeddable> embeddableClass = this.getClass();
        Properties viewResources = new Properties();
        viewResources.put("decorateNonHtml", "true");
        // Start from default ParameterConverters instance
        ParameterConverters parameterConverters = new ParameterConverters();
        // factory to allow parameter conversion and loading from external resources (used by StoryParser too)
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(), new LoadFromClasspath(embeddableClass), parameterConverters);
        // add custom coverters
        parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                new ExamplesTableConverter(examplesTableFactory));
        
        return new MostUsefulConfiguration()
            .useStoryControls(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false))
            .useStoryLoader(new LoadFromClasspath(embeddableClass))
            .useStoryParser(new RegexStoryParser(examplesTableFactory))
            .useStoryPathResolver(new UnderscoredCamelCaseResolver())
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                .withDefaultFormats()
                .withPathResolver(new ResolveToPackagedName())
                .withViewResources(viewResources)
                .withFormats(CONSOLE, TXT, HTML, XML)
                .withCrossReference(new CrossReference()))
            .useParameterConverters(parameterConverters)
            .useStepPatternParser(new RegexPrefixCapturingPatternParser(
                            "%")) // use '%' instead of '$' to identify parameters
            .useStepMonitor(new SilentStepMonitor());                               
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new TraderSteps(new TradingService()), new AndSteps(),
                new CalendarSteps(), new PriorityMatchingSteps(), new SandpitSteps(), new SearchSteps(),
                new BeforeAfterSteps(), new CompositeSteps()).createCandidateSteps();
    }
        
}
