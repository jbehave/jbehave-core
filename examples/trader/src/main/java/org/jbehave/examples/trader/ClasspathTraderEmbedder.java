package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.text.SimpleDateFormat;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.examples.trader.service.TradingService;

/**
 * Specifies the Embedder for the Trader example, providing the
 * Configuration and the CandidateSteps, using classpath story loading.
 */
public class ClasspathTraderEmbedder extends Embedder {

    @Override
    public EmbedderControls embedderControls() {
        return super.embedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
    }

    @Override
	public Configuration configuration() {
		Class<? extends ClasspathTraderEmbedder> embedderClass = this.getClass();
		return new MostUsefulConfiguration()
			.useStoryLoader(new LoadFromClasspath(embedderClass.getClassLoader()))
			.useStoryReporterBuilder(new StoryReporterBuilder()
        		.withCodeLocation(CodeLocations.codeLocationFromClass(embedderClass))
        		.withDefaultFormats()
				.withFormats(CONSOLE, TXT, HTML, XML))
            .useParameterConverters(new ParameterConverters()
                	.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")))) // use custom date pattern
            .useStepPatternParser(new RegexPrefixCapturingPatternParser(
							"%")) // use '%' instead of '$' to identify parameters
			.useStepMonitor(new SilentStepMonitor());								
	}

	@Override
	public List<CandidateSteps> candidateSteps() {
		return new InstanceStepsFactory(configuration(), new TraderSteps(
				new TradingService()), new BeforeAfterSteps())
				.createCandidateSteps();
	}
	
	
	public List<String> storyPaths() {
		return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/*.story"),
				asList(""));
	}

    public List<Embeddable> stories() {
        return asList();
    }

}