package org.jbehave.examples.trader.i18n;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.net.URL;
import java.util.Locale;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;

public class FrTraderStory extends JUnitStory {

	public FrTraderStory() {
		// use French for keywords
		ClassLoader classLoader = this.getClass().getClassLoader();
		URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
		Keywords keywords = new LocalizedKeywords(new Locale("fr"));
		Configuration configuration = new MostUsefulConfiguration()
				.useStoryPathResolver(
						new UnderscoredCamelCaseResolver(".histoire"))
				.useKeywords(keywords)
				.useStoryParser(new RegexStoryParser(keywords))
				.useStoryLoader(
						new LoadFromClasspath(classLoader))
				.useDefaultStoryReporter(new ConsoleOutput(keywords))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(codeLocation)
                    .withDefaultFormats()
                    .withFormats(CONSOLE, TXT, HTML, XML)
                    .withFailureTrace(false)
                    .withKeywords(keywords))
				.useParameterConverters(
						new ParameterConverters()
								.addConverters(new ParameterConverters.ExamplesTableConverter(
										keywords.examplesTableHeaderSeparator(),
										keywords.examplesTableValueSeparator())));
		useConfiguration(configuration);
		addSteps(new InstanceStepsFactory(configuration, new FrTraderSteps())
				.createCandidateSteps());
	}

}
