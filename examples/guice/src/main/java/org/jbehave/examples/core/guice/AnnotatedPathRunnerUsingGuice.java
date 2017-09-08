package org.jbehave.examples.core.guice;

import java.text.SimpleDateFormat;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.guice.GuiceAnnotatedPathRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.core.guice.AnnotatedPathRunnerUsingGuice.ConfigurationModule;
import org.jbehave.examples.core.guice.AnnotatedPathRunnerUsingGuice.StepsModule;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

/**
 * Run stories via annotated embedder configuration and steps using Guice. The
 * textual core stories are exactly the same ones found in the
 * jbehave-core-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(GuiceAnnotatedPathRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingGuice(modules = { ConfigurationModule.class, StepsModule.class })
@UsingPaths(searchIn = "../core/src/main/java", includes = { "**/*.story" }, excludes = { "**/examples_table*.story" })
public class AnnotatedPathRunnerUsingGuice {
    // Guice modules
    public static class ConfigurationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StoryControls.class)
                    .toInstance(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false));
            bind(StoryLoader.class).toInstance(new LoadFromClasspath(this.getClass().getClassLoader()));
            bind(ParameterConverter.class).toInstance(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")));
            bind(StoryReporterBuilder.class).toInstance(
                    new StoryReporterBuilder().withDefaultFormats().withFormats(CONSOLE, HTML, TXT, XML)
                            .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass()))
                            .withFailureTrace(true));
        }
    }

    public static class StepsModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TradingService.class).in(Scopes.SINGLETON);
            bind(GuiceCoreSteps.class).in(Scopes.SINGLETON);
            bind(BeforeAfterSteps.class).in(Scopes.SINGLETON);
            bind(AndSteps.class).in(Scopes.SINGLETON);
            bind(CalendarSteps.class).in(Scopes.SINGLETON);
            bind(PendingSteps.class).in(Scopes.SINGLETON);
            bind(PriorityMatchingSteps.class).in(Scopes.SINGLETON);
            bind(SandpitSteps.class).in(Scopes.SINGLETON);
            bind(SearchSteps.class).in(Scopes.SINGLETON);
        }
    }

}
