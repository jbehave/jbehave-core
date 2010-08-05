package org.jbehave.examples.trader.pico;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.text.SimpleDateFormat;
import java.util.List;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.pico.UsingPico;
import org.jbehave.core.configuration.pico.PicoModule;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.pico.PicoAnnotatedEmbedderRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.trader.pico.AnnotatedEmbedderUsingPico.ConfigurationModule;
import org.jbehave.examples.trader.pico.AnnotatedEmbedderUsingPico.StepsModule;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.steps.TraderSteps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;

/**
 * Run stories via annotated embedder configuration and steps using Pico. The
 * textual trader stories are exactly the same ones found in the
 * jbehave-trader-example. Here we are only concerned with using the container to
 * compose the configuration and the steps instances.
 */
@RunWith(PicoAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingPico(modules = { ConfigurationModule.class, StepsModule.class })
public class AnnotatedEmbedderUsingPico extends InjectableEmbedder {

    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        String searchInDirectory = codeLocationFromPath("../trader/src/main/java").getFile();
        return new StoryFinder().findPaths(searchInDirectory, asList("**/*.story"), null);
    }

    public static class ConfigurationModule implements PicoModule {

        public void configure(MutablePicoContainer container) {
            container.addComponent(StepPatternParser.class, new RegexPrefixCapturingPatternParser("%"));
            container.addComponent(StoryLoader.class, new LoadFromClasspath(this.getClass().getClassLoader()));
            container.addComponent(ParameterConverter.class, new DateConverter(new SimpleDateFormat("yyyy-MM-dd")));
            container.addComponent(new StoryReporterBuilder().withDefaultFormats().withFormats(CONSOLE, HTML, TXT, XML)
                    .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass())).withFailureTrace(true));
        }

    }

    public static class StepsModule implements PicoModule {

        public void configure(MutablePicoContainer container) {
            container.addComponent(TradingService.class);
            container.addComponent(TraderSteps.class);
            container.addComponent(BeforeAfterSteps.class);
            container.addComponent(AndSteps.class);
            container.addComponent(CalendarSteps.class);
            container.addComponent(PriorityMatchingSteps.class);
            container.addComponent(SandpitSteps.class);
            container.addComponent(SearchSteps.class);
        }

    }

}
