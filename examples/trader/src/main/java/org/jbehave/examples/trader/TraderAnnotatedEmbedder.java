package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.text.SimpleDateFormat;

import org.jbehave.core.Embeddable;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.examples.trader.TraderAnnotatedEmbedder.MyDateConverter;
import org.jbehave.examples.trader.TraderAnnotatedEmbedder.MyReportBuilder;
import org.jbehave.examples.trader.TraderAnnotatedEmbedder.MyStoryLoader;
import org.jbehave.examples.trader.stories.AndStep.AndSteps;
import org.jbehave.examples.trader.stories.ClaimsWithNullCalendar.CalendarSteps;
import org.jbehave.examples.trader.stories.FailureFollowedByGivenStories.SandpitSteps;
import org.jbehave.examples.trader.stories.PriorityMatching.PriorityMatchingSteps;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnnotatedEmbedderRunner.class)
@Configure(storyLoader = MyStoryLoader.class, storyReporterBuilder = MyReportBuilder.class, 
        parameterConverters = { MyDateConverter.class })
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingSteps(instances = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class })
public class TraderAnnotatedEmbedder implements Embeddable {

    private Embedder embedder;

    public void useEmbedder(Embedder embedder) {
        this.embedder = embedder;
    }

    @Test
    public void run() {
        embedder.runStoriesAsPaths(new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(),
                asList("**/*.story"), asList("")));
    }

    public static class MyReportBuilder extends StoryReporterBuilder {
        public MyReportBuilder() {
            this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats();
        }
    }

    public static class MyStoryLoader extends LoadFromClasspath {
        public MyStoryLoader() {
            super(TraderAnnotatedEmbedder.class.getClassLoader());
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
