package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.text.SimpleDateFormat;

import org.jbehave.core.annotations.WithCandidateSteps;
import org.jbehave.core.annotations.WithConfiguration;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathFinder;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.examples.trader.stories.AndStep.AndSteps;
import org.jbehave.examples.trader.stories.ClaimsWithNullCalendar.CalendarSteps;
import org.jbehave.examples.trader.stories.FailureFollowedByGivenStories.SandpitSteps;
import org.jbehave.examples.trader.stories.PriorityMatching.PriorityMatchingSteps;
import org.junit.Test;

@WithConfiguration(stepMonitor = SilentStepMonitor.class, 
        stepPatternParser = AnnotatedTraderStoryRunner.MyRegexPrefixCapturingPatternParser.class, 
        storyLoader = AnnotatedTraderStoryRunner.MyStoryLoader.class, 
        storyReporterBuilder = AnnotatedTraderStoryRunner.MyReportBuilder.class, 
        parameterConverters = { AnnotatedTraderStoryRunner.MyDateConverter.class })
@WithCandidateSteps(candidateSteps = { TraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class, 
        PriorityMatchingSteps.class, SandpitSteps.class })
public class AnnotatedTraderStoryRunner {

    @Test
    public void run() {
        Embedder embedder = new Embedder();
        embedder.useConfiguration(new AnnotationBuilder().buildConfiguration(this));
        embedder.useCandidateSteps(new AnnotationBuilder().buildCandidateSteps(this));
        embedder.embedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
        embedder.runStoriesAsPaths(new StoryPathFinder().listStoryPaths("target/classes", "", asList("**/*.story"),
                asList("")));
    }

    public static class MyReportBuilder extends StoryReporterBuilder {
        public MyReportBuilder() {
            this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats();
        }
    }

    public static class MyStoryLoader extends LoadFromClasspath {
        public MyStoryLoader() {
            super(AnnotatedTraderStoryRunner.class.getClassLoader());
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
