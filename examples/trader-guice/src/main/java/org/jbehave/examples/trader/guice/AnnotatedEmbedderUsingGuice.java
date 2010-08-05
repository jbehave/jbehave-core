package org.jbehave.examples.trader.guice;

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
import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.guice.GuiceAnnotatedEmbedderRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.guice.AnnotatedEmbedderUsingGuice.ConfigurationModule;
import org.jbehave.examples.trader.guice.AnnotatedEmbedderUsingGuice.StepsModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Run stories via annotated embedder configuration and steps using Guice. The
 * textual trader stories are exactly the same ones found in the
 * jbehave-trader-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(GuiceAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingGuice(modules = { ConfigurationModule.class, StepsModule.class })
public class AnnotatedEmbedderUsingGuice extends InjectableEmbedder {

    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        String searchInDirectory = codeLocationFromPath("../trader/src/main/java").getFile();
        return new StoryFinder().findPaths(searchInDirectory, asList("**/*.story"), null);
    }

    // Guice modules
    public static class ConfigurationModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(StepPatternParser.class).toInstance(new RegexPrefixCapturingPatternParser("%"));
            bind(StoryLoader.class).toInstance(new LoadFromClasspath(this.getClass().getClassLoader()));
            bind(ParameterConverter.class).toInstance(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")));
            bind(StoryReporterBuilder.class).toInstance(
                    new StoryReporterBuilder().withDefaultFormats().withFormats(CONSOLE, HTML, TXT, XML)
                            .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass())).withFailureTrace(
                                    true));
        }

    }

    public static class StepsModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(TradingService.class).in(Scopes.SINGLETON);
            bind(GuiceTraderSteps.class).in(Scopes.SINGLETON);
            bind(BeforeAfterSteps.class).in(Scopes.SINGLETON);
            bind(AndSteps.class).in(Scopes.SINGLETON);
            bind(CalendarSteps.class).in(Scopes.SINGLETON);
            bind(PriorityMatchingSteps.class).in(Scopes.SINGLETON);
            bind(SandpitSteps.class).in(Scopes.SINGLETON);
            bind(SearchSteps.class).in(Scopes.SINGLETON);
        }

    }

}
