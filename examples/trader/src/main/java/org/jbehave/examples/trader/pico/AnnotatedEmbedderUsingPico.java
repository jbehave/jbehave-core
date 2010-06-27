package org.jbehave.examples.trader.pico;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.text.SimpleDateFormat;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.pico.UsingPico;
import org.jbehave.core.configuration.pico.PicoAnnotationBuilder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.pico.AnnotatedEmbedderUsingPico.TraderPicoContainer;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.stories.AndStep.AndSteps;
import org.jbehave.examples.trader.stories.ClaimsWithNullCalendar.CalendarSteps;
import org.jbehave.examples.trader.stories.FailureFollowedByGivenStories.SandpitSteps;
import org.jbehave.examples.trader.stories.PriorityMatching.PriorityMatchingSteps;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

/**
 * Run stories via Embedder using JBehave's annotated configuration using Pico
 * injection
 */
@Configure()
@UsingEmbedder()
@UsingPico(containers = { TraderPicoContainer.class })
public class AnnotatedEmbedderUsingPico {

    @Test
    public void run() {
        PicoAnnotationBuilder builder = new PicoAnnotationBuilder(this.getClass());
        Embedder embedder = builder.buildEmbedder();
        embedder.embedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
        embedder.runStoriesAsPaths(new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(),
                asList("**/stories/*.story"), asList("")));
    }

    @SuppressWarnings("serial")
    public static class TraderPicoContainer extends DefaultPicoContainer {

        public TraderPicoContainer() {
            super(new Caching().wrap(new ConstructorInjection()));
            addComponent(StepPatternParser.class, new RegexPrefixCapturingPatternParser("%"));
            addComponent(StoryLoader.class, new LoadFromClasspath(this.getClass().getClassLoader()));
            addComponent(ParameterConverter.class, new DateConverter(new SimpleDateFormat("yyyy-MM-dd")));
            addComponent(new StoryReporterBuilder().withDefaultFormats().withFormats(CONSOLE, HTML, TXT, XML)
                    .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass())).withFailureTrace(true));
            addComponent(TradingService.class);
            addComponent(TraderSteps.class);
            addComponent(BeforeAfterSteps.class);
            addComponent(AndSteps.class);
            addComponent(CalendarSteps.class);
            addComponent(PriorityMatchingSteps.class);
            addComponent(SandpitSteps.class);
        }

    }

}
