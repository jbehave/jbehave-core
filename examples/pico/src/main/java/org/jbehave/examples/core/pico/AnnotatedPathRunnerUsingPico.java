package org.jbehave.examples.core.pico;

import java.text.SimpleDateFormat;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.pico.UsingPico;
import org.jbehave.core.configuration.pico.PicoModule;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.junit.pico.PicoAnnotatedPathRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.core.pico.AnnotatedPathRunnerUsingPico.ConfigurationModule;
import org.jbehave.examples.core.pico.AnnotatedPathRunnerUsingPico.StepsModule;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.TraderSteps;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

/**
 * Run stories via annotated embedder configuration and steps using Pico. The
 * textual core stories are exactly the same ones found in the
 * jbehave-core-example. Here we are only concerned with using the container to
 * compose the configuration and the steps instances.
 */
@RunWith(PicoAnnotatedPathRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingPico(modules = { ConfigurationModule.class, StepsModule.class })
@UsingPaths(searchIn = "../core/src/main/java", includes = { "**/*.story" }, excludes = { "**/examples_table*.story" })
public class AnnotatedPathRunnerUsingPico {

    public static class ConfigurationModule implements PicoModule {

        public void configure(MutablePicoContainer container) {
            container.addComponent(StoryControls.class, new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false));
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
            container.addComponent(PendingSteps.class);
            container.addComponent(PriorityMatchingSteps.class);
            container.addComponent(SandpitSteps.class);
            container.addComponent(SearchSteps.class);
        }

    }

}
