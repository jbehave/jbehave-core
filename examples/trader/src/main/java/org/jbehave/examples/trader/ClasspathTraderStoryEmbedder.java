package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.util.List;

import org.jbehave.core.StoryEmbedder;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathFinder;
import org.jbehave.core.parsers.PrefixCapturingRegexPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepsConfiguration;
import org.jbehave.core.steps.StepsFactory;
import org.jbehave.examples.trader.converters.TraderConverter;
import org.jbehave.examples.trader.model.Stock;
import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.persistence.TraderPersister;
import org.jbehave.examples.trader.service.TradingService;

/**
 * Specifies the StoryEmbedder for the Trader example, providing the
 * StoryConfiguration and the CandidateSteps, using classpath story loading.
 */
public class ClasspathTraderStoryEmbedder extends StoryEmbedder {

	@Override
	public StoryConfiguration configuration() {
		Class<? extends ClasspathTraderStoryEmbedder> embedderClass = this.getClass();
		return new MostUsefulStoryConfiguration()
			.useStoryLoader(new LoadFromClasspath(embedderClass.getClassLoader()))
			.useStoryReporterBuilder(new StoryReporterBuilder()
        		// use absolute output directory with Ant
        		//.outputTo("target/jbehave-reports").outputAsAbsolute(true)
        		.outputLocationClass(embedderClass)
        		.withDefaultFormats()
				.withFormats(CONSOLE, TXT, HTML, XML))
			.buildReporters(storyPaths());
	}

	@Override
	public List<CandidateSteps> candidateSteps() {
		// start with default steps configuration, overriding parameter
		// converters, pattern builder and monitor
		StepsConfiguration stepsConfiguration = new MostUsefulStepsConfiguration()
			.useParameterConverters(new ParameterConverters(
				new TraderConverter(mockTradePersister())))
			.usePatternParser(new PrefixCapturingRegexPatternParser(
				"%")) // use '%' instead of '$' to identify parameters
			.useMonitor(new SilentStepMonitor());
		return asList(new StepsFactory(stepsConfiguration)
				.createCandidateSteps(new TraderSteps(new TradingService()),
						new BeforeAfterSteps()));
	}

	protected TraderPersister mockTradePersister() {
		return new TraderPersister(new Trader("Mauro", asList(new Stock("STK1",
				10.d))));
	}

	public List<String> storyPaths() {
		StoryPathFinder finder = new StoryPathFinder();
		return finder.listStoryPaths("target/classes", "",
				asList("**/*.story"), asList(""));
	}

}