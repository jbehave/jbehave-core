package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.i18n.StringCoder;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepsFactory;

public class ItTraderStory extends JUnitStory {

    public ItTraderStory() {
        StoryConfiguration storyConfiguration = new MostUsefulStoryConfiguration();
        storyConfiguration.useStoryPathResolver(new UnderscoredCamelCaseResolver(".story"));
        ClassLoader classLoader = this.getClass().getClassLoader();
        Keywords keywords = new LocalizedKeywords(new Locale("it"), new StringCoder(), "org/jbehave/examples/trader/i18n/keywords", classLoader);
        // use Italian for keywords
        storyConfiguration.useKeywords(keywords);
        storyConfiguration.useStoryParser(new RegexStoryParser(storyConfiguration.keywords()));
        storyConfiguration.useStoryLoader(new LoadFromClasspath(this.getClass().getClassLoader()));
        storyConfiguration.useStoryReporter(new ConsoleOutput(storyConfiguration.keywords()));
        useConfiguration(storyConfiguration);

        StoryConfiguration stepsConfiguration = new MostUsefulStoryConfiguration();
        // use Italian for keywords
        stepsConfiguration.useKeywords(keywords);
        stepsConfiguration.useParameterConverters(new ParameterConverters(new ParameterConverters.ExamplesTableConverter(keywords.examplesTableHeaderSeparator(), keywords.examplesTableValueSeparator())));
        addSteps(new StepsFactory(stepsConfiguration).createCandidateSteps(new ItTraderSteps()));
    }

}
