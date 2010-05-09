package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.util.List;

import org.jbehave.core.MostUsefulStoryConfiguration;
import org.jbehave.core.StoryConfiguration;
import org.jbehave.core.StoryEmbedder;
import org.jbehave.core.parser.LoadFromClasspath;
import org.jbehave.core.parser.PrefixCapturingPatternBuilder;
import org.jbehave.core.parser.StoryPathFinder;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepMonitor;
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
		return new MostUsefulStoryConfiguration()
			.useStoryLoader(new LoadFromClasspath(this.getClass().getClassLoader()))
			.useStoryReporterBuilder(new StoryReporterBuilder()
				.outputLocationClass(this.getClass())
				.withDefaultFormats()
				.withFormats(CONSOLE, TXT, HTML, XML))
			.buildReporters(storyPaths());
	}

	@Override
	public List<CandidateSteps> candidateSteps() {
		// start with default steps configuration, overriding parameter
		// converters, pattern builder and monitor
		StepsConfiguration stepsConfiguration = new MostUsefulStepsConfiguration();
		StepMonitor monitor = new SilentStepMonitor();
		stepsConfiguration.useParameterConverters(new ParameterConverters(
				monitor, new TraderConverter(mockTradePersister())));
		stepsConfiguration.usePatternBuilder(new PrefixCapturingPatternBuilder(
				"%")); // use '%' instead of '$' to identify parameters
		stepsConfiguration.useMonitor(monitor);
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