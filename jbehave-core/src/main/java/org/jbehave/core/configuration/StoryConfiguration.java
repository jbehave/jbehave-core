package org.jbehave.core.configuration;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * <p>
 * Provides the story configuration used by the {@link Embedder} and the in the
 * {@link RunnableStory} implementations to customise its runtime properties.
 * </p>
 * <p>
 * StoryConfiguration implements a <a
 * href="http://en.wikipedia.org/wiki/Builder_pattern">Builder</a> pattern so
 * that each element of the configuration can be specified individually, and
 * read well. All elements have default values, which can be overridden by the
 * "use" methods. The "use" methods allow to override the dependencies one by
 * one and play nicer with a Java hierarchical structure, in that does allow the
 * use of non-static member variables.
 * </p>
 */
public class StoryConfiguration {

	/**
	 * Use English language for keywords
	 */
	private Keywords keywords = new LocalizedKeywords();

	/**
	 * Provides pending steps where unmatched steps exist.
	 */
	private StepCollector stepCollector = new MarkUnmatchedStepsAsPending();

	/**
	 * Parses the textual representation via pattern matching of keywords
	 */
	private StoryParser storyParser = new RegexStoryParser(keywords);

	/**
	 * Loads story content from classpath
	 */
	private StoryLoader storyLoader = new LoadFromClasspath();

	/**
	 * Resolves story paths from class names using underscored camel case with
	 * ".story" extension
	 */
	private StoryPathResolver storyPathResolver = new UnderscoredCamelCaseResolver();

	/**
	 * Handles errors by re-throwing them.
	 * <p/>
	 * If there are multiple scenarios in a single story, this could cause the
	 * story to stop after the first failing scenario.
	 * <p/>
	 * Users wanting a different behaviour may use {@link SilentlyAbsorbingFailure}.
	 */
	private FailureStrategy failureStrategy = new RethrowingFailure();

	/**
	 * Allows pending steps to pass, so that steps that to do not match any
	 * method will not cause failure.
	 * <p/>
	 * Uses wanting a stricter behaviour for pending steps may use
	 * {@link FailingUponPendingStep}.
	 */
	private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();

	/**
	 * Reports stories to console output
	 */
	private StoryReporter storyReporter = new ConsoleOutput();

	/**
	 * Collects story reporters by story path
	 */
	private Map<String, StoryReporter> storyReporters = new HashMap<String, StoryReporter>();

	/**
	 * The story reporter builder
	 */
	private StoryReporterBuilder storyReporterBuilder = new StoryReporterBuilder();

	/**
	 * The embedder configuration
	 */
	private EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();

	/**
	 * Pattern build that uses prefix for identifying parameters
	 */
	private StepPatternParser stepPatternParser = new RegexPrefixCapturingPatternParser();

	/**
	 * Silent monitoring that does not produce any noise of the step matching.
	 * </p> If needed, users can switch on verbose monitoring using
	 * {@link PrintStreamStepMonitor}
	 */
	private StepMonitor stepMonitor = new SilentStepMonitor();

	/**
	 * Paranamer use is switched off by default
	 */
	private Paranamer paranamer = new NullParanamer();

	/**
	 * Use default built-in parameter converters
	 */
	private ParameterConverters parameterConverters = new ParameterConverters();

	/**
	 * Dry run is switched off by default
	 */
	private boolean dryRun = false;

	public StepCollector stepCollector() {
		return stepCollector;
	}

	public StoryParser storyParser() {
		return storyParser;
	}

	public StoryLoader storyLoader() {
		return storyLoader;
	}

	public StoryPathResolver storyPathResolver() {
		return storyPathResolver;
	}

	public FailureStrategy failureStrategy() {
		return failureStrategy;
	}

	public PendingStepStrategy pendingStepStrategy() {
		return pendingStepStrategy;
	}

	public StoryReporter storyReporter() {
		return storyReporter;
	}

	public StoryReporter storyReporter(String storyPath) {
		StoryReporter storyReporter = storyReporters.get(storyPath);
		if (storyReporter != null) {
			return storyReporter;
		}
		// default to configured story reporter
		// TODO consider merging the two methods
		return storyReporter();
	}

	public StoryReporterBuilder storyReporterBuilder() {
		return storyReporterBuilder;
	}

	public Keywords keywords() {
		return keywords;
	}

	public EmbedderConfiguration embedderConfiguration() {
		return embedderConfiguration;
	}

	public StoryConfiguration useKeywords(Keywords keywords) {
		this.keywords = keywords;
		return this;
	}

	public StoryConfiguration useStepCollector(StepCollector stepCollector) {
		this.stepCollector = stepCollector;
		return this;
	}

	public StoryConfiguration usePendingStepStrategy(
			PendingStepStrategy pendingStepStrategy) {
		this.pendingStepStrategy = pendingStepStrategy;
		return this;
	}

	public StoryConfiguration useErrorStrategy(FailureStrategy failureStrategy) {
		this.failureStrategy = failureStrategy;
		return this;
	}

	public StoryConfiguration useStoryParser(StoryParser storyParser) {
		this.storyParser = storyParser;
		return this;
	}

	public StoryConfiguration useStoryLoader(StoryLoader storyLoader) {
		this.storyLoader = storyLoader;
		return this;
	}

	public StoryConfiguration useStoryPathResolver(
			StoryPathResolver storyPathResolver) {
		this.storyPathResolver = storyPathResolver;
		return this;
	}

	public StoryConfiguration useStoryReporter(StoryReporter storyReporter) {
		this.storyReporter = storyReporter;
		return this;
	}

	public StoryConfiguration useStoryReporter(String storyPath,
			StoryReporter storyReporter) {
		this.storyReporters.put(storyPath, storyReporter);
		return this;
	}

	public StoryConfiguration useStoryReporters(
			Map<String, StoryReporter> storyReporters) {
		this.storyReporters.putAll(storyReporters);
		return this;
	}

	public StoryConfiguration useStoryReporterBuilder(
			StoryReporterBuilder storyReporterBuilder) {
		this.storyReporterBuilder = storyReporterBuilder;
		return this;
	}

	public StoryConfiguration useEmbedderConfiguration(
			EmbedderConfiguration embedderConfiguration) {
		this.embedderConfiguration = embedderConfiguration;
		return this;
	}

	public StoryConfiguration buildReporters(String... storyPaths) {
		return buildReporters(asList(storyPaths));
	}

	public StoryConfiguration buildReporters(List<String> storyPaths) {
		this.storyReporters.putAll(storyReporterBuilder.build(storyPaths));
		return this;
	}

	public StepPatternParser stepPatternParser() {
		return stepPatternParser;
	}

	public StoryConfiguration useStepPatternParser(
			StepPatternParser stepPatternParser) {
		this.stepPatternParser = stepPatternParser;
		return this;
	}

	public StepMonitor stepMonitor() {
		return stepMonitor;
	}

	public StoryConfiguration useStepMonitor(StepMonitor stepMonitor) {
		this.stepMonitor = stepMonitor;
		return this;
	}

	public Paranamer paranamer() {
		return paranamer;
	}

	public StoryConfiguration useParanamer(Paranamer paranamer) {
		this.paranamer = paranamer;
		return this;
	}

	public ParameterConverters parameterConverters() {
		return parameterConverters;
	}

	public StoryConfiguration useParameterConverters(
			ParameterConverters parameterConverters) {
		this.parameterConverters = parameterConverters;
		return this;
	}

	public boolean dryRun() {
		return dryRun;
	}

	public void doDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
