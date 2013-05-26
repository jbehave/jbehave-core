package org.jbehave.examples.core.annotations;

import java.text.SimpleDateFormat;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.examples.core.annotations.CoreAnnotatedPathRunner.MyDateConverter;
import org.jbehave.examples.core.annotations.CoreAnnotatedPathRunner.MyRegexPrefixCapturingPatternParser;
import org.jbehave.examples.core.annotations.CoreAnnotatedPathRunner.MyReportBuilder;
import org.jbehave.examples.core.annotations.CoreAnnotatedPathRunner.MyStoryControls;
import org.jbehave.examples.core.annotations.CoreAnnotatedPathRunner.MyStoryLoader;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.TraderSteps;
import org.junit.runner.RunWith;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

@RunWith(AnnotatedPathRunner.class)
@Configure(stepPatternParser = MyRegexPrefixCapturingPatternParser.class, storyControls = MyStoryControls.class, storyLoader = MyStoryLoader.class, storyReporterBuilder = MyReportBuilder.class, 
        parameterConverters = { MyDateConverter.class } )
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true,
                storyTimeoutInSecs = 100, threads = 1, metaFilters = "-skip", systemProperties="java.awt.headless=true")
@UsingSteps(instances = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class, SearchSteps.class })
@UsingPaths(searchIn = "../core/src/main/java", includes = { "**/*.story" }, excludes = { "**/examples_table*.story", "**/given_relative_path*" })
public class CoreAnnotatedPathRunner {

    public static class MyStoryControls extends StoryControls {
        public MyStoryControls() {
            doDryRun(false);
            doSkipScenariosAfterFailure(false);
        }
    }

    public static class MyStoryLoader extends LoadFromClasspath {
        public MyStoryLoader() {
            super(CoreAnnotatedPathRunner.class.getClassLoader());
        }
    }

    public static class MyReportBuilder extends StoryReporterBuilder {
        public MyReportBuilder() {
            this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats();
        }
    }

    public static class MyRegexPrefixCapturingPatternParser extends RegexPrefixCapturingPatternParser {
        public MyRegexPrefixCapturingPatternParser() {
            super("%");
        }
    }

    public static class MyDateConverter extends DateConverter {
        public MyDateConverter() {
            super(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }

}
