package org.jbehave.examples.trader;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.StoryConfiguration;
import org.jbehave.core.parser.LoadFromClasspath;
import org.jbehave.core.parser.PrefixCapturingPatternBuilder;
import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.StepsConfiguration;
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.jbehave.examples.trader.converters.TraderConverter;
import org.jbehave.examples.trader.model.Stock;
import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.persistence.TraderPersister;
import org.jbehave.examples.trader.service.TradingService;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

import static java.util.Arrays.asList;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

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

        useConfiguration(new StoryConfiguration()
                .useStoryLoader(new LoadFromClasspath(storyClass.getClassLoader()))
                .useStoryReporter(new StoryReporterBuilder()
                // use absolute output with Ant, as the code source location
                // defaults to $ANT_HOME/lib
                // .outputTo("target/jbehave-reports").outputAsAbsolute(true)
                        .outputLocationClass(storyClass).withDefaultFormats()
                        .withFormats(CONSOLE, TXT, HTML, XML).build(storyPath))
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
		addSteps(createSteps(stepsConfiguration));
	}

    protected CandidateSteps[] createSteps(StepsConfiguration configuration) {
        PicoContainer parent = createPicoContainer();
        return new PicoStepsFactory(configuration, parent).createCandidateSteps();
    }

    private PicoContainer createPicoContainer() {
        MutablePicoContainer parent = new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
        parent.as(Characteristics.USE_NAMES).addComponent(TradingService.class);
        parent.as(Characteristics.USE_NAMES).addComponent(TraderSteps.class);
        parent.as(Characteristics.USE_NAMES).addComponent(BeforeAfterSteps.class);
        return parent;
    }
	private TraderPersister mockTradePersister() {
		return new TraderPersister(new Trader("Mauro", asList(new Stock("STK1",
				10.d))));
	}

}
