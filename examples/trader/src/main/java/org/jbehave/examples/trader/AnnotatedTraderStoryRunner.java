package org.jbehave.examples.trader;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import org.jbehave.core.annotations.WithCandidateSteps;
import org.jbehave.core.annotations.WithConfiguration;
import org.jbehave.core.configuration.AnnotatedConfigurationFactory;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.AnnotatedStepsFactory;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.junit.Test;

@WithConfiguration(stepMonitor = PrintStreamStepMonitor.class, parameterConverters = { DateConverter.class }, stepPatternParser = AnnotatedTraderStoryRunner.MyRegexPrefixCapturingPatternParser.class, storyReporterBuilder = AnnotatedTraderStoryRunner.MyReportBuilder.class)
@WithCandidateSteps(candidateSteps = { TraderSteps.class })
public class AnnotatedTraderStoryRunner {

	@Test
	public void run() {
		Embedder embedder = new Embedder();
		embedder.useConfiguration(new AnnotatedConfigurationFactory(this)
				.createConfiguration());
		embedder.useCandidateSteps(new AnnotatedStepsFactory(this)
				.createCandidateSteps());
		embedder.embedderControls().doIgnoreFailureInStories(true);
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

	public static class MyRegexPrefixCapturingPatternParser extends
			RegexPrefixCapturingPatternParser {
		public MyRegexPrefixCapturingPatternParser() {
			super("%");
		}
	}
}
