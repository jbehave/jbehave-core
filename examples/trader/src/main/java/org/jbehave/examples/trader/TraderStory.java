package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.parsers.PrefixCapturingPatternBuilder;
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
 * Example of how to run a story using a JBehave2 style inheritance. A story
 * just need to extend this abstract class and are out-of-the-box runnable via
 * JUnit.
 */
public abstract class TraderStory extends JUnitStory {

	public TraderStory() {

		// start with default story configuration, overriding story loader and reporter
        StoryPathResolver storyPathResolver = new UnderscoredCamelCaseResolver(".story");
        Class<? extends TraderStory> storyClass = this.getClass();
        String storyPath = storyPathResolver.resolve(storyClass);

        useConfiguration(new MostUsefulStoryConfiguration()
                .useStoryLoader(new LoadFromClasspath(storyClass.getClassLoader()))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                	// use absolute output directory with Ant
                	//.outputTo("target/jbehave-reports").outputAsAbsolute(true)
                	.outputLocationClass(storyClass)
                	.withDefaultFormats()
                	.withFormats(CONSOLE, TXT, HTML, XML))
                .buildReporters(storyPath)
                .useStoryPathResolver(storyPathResolver));

		// start with default steps configuration, overriding parameter
		// converters, pattern builder and monitor
		StepsConfiguration stepsConfiguration = new MostUsefulStepsConfiguration();
		StepMonitor monitor = new SilentStepMonitor();
		stepsConfiguration.useParameterConverters(new ParameterConverters(
				monitor, new TraderConverter(mockTradePersister()))); 
		stepsConfiguration.usePatternBuilder(new PrefixCapturingPatternBuilder(
				"%")); // use '%' instead of '$' to identify parameters
		stepsConfiguration.useMonitor(monitor);
		stepsConfiguration.doDryRun(false);
		addSteps(createSteps(stepsConfiguration));
	}

	protected CandidateSteps[] createSteps(StepsConfiguration configuration) {
		return new StepsFactory(configuration).createCandidateSteps(
				new TraderSteps(new TradingService()), new BeforeAfterSteps());
	}

	private TraderPersister mockTradePersister() {
		return new TraderPersister(new Trader("Mauro", asList(new Stock("STK1",
				10.d))));
	}

}
