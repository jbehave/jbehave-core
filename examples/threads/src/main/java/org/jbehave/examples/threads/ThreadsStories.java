package org.jbehave.examples.threads;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.embedder.StoryTimeouts.TimeoutParser;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JBehaveJUnit4Runner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.threads.steps.ThreadsSteps;
import org.junit.runner.RunWith;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

@RunWith(JBehaveJUnit4Runner.class)
public class ThreadsStories extends JUnitStories {

    public ThreadsStories() {
        Embedder embedder = configuredEmbedder();
        embedder.embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true).doVerboseFiltering(true).useThreads(1).useStoryTimeouts("7secs").doFailOnStoryTimeout(false);
        embedder.useMetaFilters(Arrays.asList("groovy: story_path ==~ /.*long.*/"));
        embedder.useTimeoutParsers(new CustomTimeoutParser());
    }

    @Override
    public Configuration configuration() {
        Class<? extends Embeddable> embeddableClass = this.getClass();
        return new MostUsefulConfiguration().useStoryLoader(new LoadFromClasspath(embeddableClass))
                .useStoryControls(new StoryControls().useStoryMetaPrefix("story_").useScenarioMetaPrefix("scenario_")) // optional prefixes
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                                .withDefaultFormats().withFormats(CONSOLE, HTML).withFailureTrace(true)
                                .withFailureTraceCompression(true));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new ThreadsSteps());
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
    }
    
    public static class CustomTimeoutParser implements TimeoutParser {

        @Override
        public boolean isValid(String timeout) {
            return timeout.matches("(\\d+)secs");
        }

        @Override
        public long asSeconds(String timeout) {
            return Long.parseLong(StringUtils.substringBefore(timeout, "secs"));
        }

    }


}
