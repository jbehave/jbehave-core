package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.i18n.StringCoder;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;

public class ItTraderStory extends JUnitStory {

    public ItTraderStory() {
        // use Italian for keywords
        ClassLoader classLoader = this.getClass().getClassLoader();
        Keywords keywords = new LocalizedKeywords(new Locale("it"), new StringCoder(), "org/jbehave/examples/trader/i18n/keywords", classLoader);
        Configuration configuration = new MostUsefulConfiguration()
        	.useStoryPathResolver(new UnderscoredCamelCaseResolver(".story"))
        	.useKeywords(keywords)
        	.useStoryParser(new RegexStoryParser(keywords))
        	.useStoryLoader(new LoadFromClasspath(this.getClass().getClassLoader()))
        	.useDefaultStoryReporter(new ConsoleOutput(keywords))
        	.useKeywords(keywords)
        	.useParameterConverters(new ParameterConverters(new ParameterConverters.ExamplesTableConverter(keywords.examplesTableHeaderSeparator(), keywords.examplesTableValueSeparator())));
        useConfiguration(configuration);
        addSteps(new InstanceStepsFactory(configuration, new ItTraderSteps()).createCandidateSteps());
    }

}
