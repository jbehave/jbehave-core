package org.jbehave.examples.core.annotations;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

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
import org.junit.runner.RunWith;

@RunWith(AnnotatedPathRunner.class)
@Configure(stepPatternParser = MyRegexPrefixCapturingPatternParser.class, storyControls = MyStoryControls.class, storyLoader = MyStoryLoader.class, storyReporterBuilder = MyReportBuilder.class, 
parameterConverters = { MyDateConverter.class } )
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true,
                storyTimeouts = "100", threads = 1, metaFilters = "-skip", systemProperties = "java.awt.headless=true")
@UsingSteps(packages = "org.jbehave.examples.core.steps", notMatchingNames = ".*Failing.*")
@UsingPaths(searchIn = "../core/src/main/java", includes = { "**/*.story" }, excludes = { "**/examples_table*.story", "**/failing*.story", "**/given_relative_path*" })
public class CoreAnnotatedPathRunnerUsingScanning {

    public static class MyStoryControls extends StoryControls {
        public MyStoryControls() {
            doDryRun(false);
            doSkipScenariosAfterFailure(false);
        }
    }

    public static class MyStoryLoader extends LoadFromClasspath {
        public MyStoryLoader() {
            super(CoreAnnotatedPathRunnerUsingScanning.class.getClassLoader());
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
