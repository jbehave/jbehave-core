package org.jbehave.examples.trader.stories;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.examples.trader.TraderStory;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.FailingBeforeAfterStoriesSteps;
import org.jbehave.examples.trader.steps.TraderSteps;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

public class FailingBeforeStories extends TraderStory {
    // overriding configuration to not use CrossReference which does not handle steps which are not tied to a Story
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
                .withFailureTrace(true)
                .withFailureTraceCompression(true))
                //.withCrossReference(xref))  
            .useParameterConverters(parameterConverters)
            .useStepPatternParser(new RegexPrefixCapturingPatternParser(
                            "%")); // use '%' instead of '$' to identify parameters
            //.useStepMonitor(xref.getStepMonitor());                               
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new TraderSteps(new TradingService()), 
                new FailingBeforeAfterStoriesSteps()).createCandidateSteps();
    }
        
}
