package org.jbehave.core.configuration;

import org.jbehave.core.Embeddable;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.AbsolutePathCalculator;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.PathCalculator;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * <p>
 * Provides the configuration used by the {@link Embedder} and the in the
 * {@link Embeddable} implementations to customise its runtime properties.
 * </p>
 * <p>
 * Configuration implements a <a
 * href="http://en.wikipedia.org/wiki/Builder_pattern">Builder</a> pattern so
 * that each element of the configuration can be specified individually, and
 * read well. All elements have default values, which can be overridden by the
 * "use" methods. The "use" methods allow to override the dependencies one by
 * one and play nicer with a Java hierarchical structure, in that does allow the
 * use of non-static member variables.
 * </p>
 */
public abstract class Configuration {

    /**
     * Use default story controls
     */
    private StoryControls storyControls = new StoryControls();

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
     * Users wanting a different behaviour may use
     * {@link SilentlyAbsorbingFailure}.
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
    private StoryReporter defaultStoryReporter = new ConsoleOutput();

    /**
     * The story reporter builder
     */
    private StoryReporterBuilder storyReporterBuilder = new StoryReporterBuilder();

    /**
     * Finder of matching candidate steps
     */
    private StepFinder stepFinder = new StepFinder();

    /**
     * Report candidate steps found to System.out
     */
    private StepdocReporter stepdocReporter = new PrintStreamStepdocReporter();

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
     * Paranamer is switched off by default
     */
    private Paranamer paranamer = new NullParanamer();

    /**
     * Use default built-in parameter converters
     */
    private ParameterConverters parameterConverters = new ParameterConverters();

    /**
     * Use Freemarker-based view generator
     */
    private ViewGenerator viewGenerator = new FreemarkerViewGenerator();

    private PathCalculator pathCalculator = new AbsolutePathCalculator();

    public Keywords keywords() {
        return keywords;
    }

    public boolean dryRun() {
        return storyControls.dryRun();
    }
    
    public StoryControls storyControls() {
        return storyControls;
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

    /**
     * @deprecated Use {@link StoryReporterBuilder}
     */
    public StoryReporter defaultStoryReporter() {
        return defaultStoryReporter;
    }

    public StoryReporter storyReporter(String storyPath) {
        return storyReporterBuilder.build(storyPath);
    }

    public StoryReporterBuilder storyReporterBuilder() {
        return storyReporterBuilder;
    }

    public StepCollector stepCollector() {
        return stepCollector;
    }

    public StepFinder stepFinder() {
        return stepFinder;
    }

    public StepdocReporter stepdocReporter() {
        return stepdocReporter;
    }

    public StepPatternParser stepPatternParser() {
        return stepPatternParser;
    }

    public StepMonitor stepMonitor() {
        return stepMonitor;
    }

    public Paranamer paranamer() {
        return paranamer;
    }

    public ParameterConverters parameterConverters() {
        return parameterConverters;
    }
    
    public ViewGenerator viewGenerator() {
        return viewGenerator;
    }

    public PathCalculator pathCalculator() {
        return pathCalculator;
    }

    public Configuration useKeywords(Keywords keywords) {
        this.keywords = keywords;
        return this;
    }

    public Configuration doDryRun(Boolean dryRun) {
        this.storyControls.doDryRun(dryRun);
        return this;
    }
    
    public Configuration useStoryControls(StoryControls storyControls){
        this.storyControls = storyControls;
        return this;
    }
    
    public Configuration usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
        this.pendingStepStrategy = pendingStepStrategy;
        return this;
    }

    public Configuration useFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
        return this;
    }

    public Configuration useStoryParser(StoryParser storyParser) {
        this.storyParser = storyParser;
        return this;
    }

    public Configuration useStoryLoader(StoryLoader storyLoader) {
        this.storyLoader = storyLoader;
        return this;
    }

    public Configuration useStoryPathResolver(StoryPathResolver storyPathResolver) {
        this.storyPathResolver = storyPathResolver;
        return this;
    }

    public Configuration useDefaultStoryReporter(StoryReporter storyReporter) {
        this.defaultStoryReporter = storyReporter;
        return this;
    }

    public Configuration useStoryReporterBuilder(StoryReporterBuilder storyReporterBuilder) {
        this.storyReporterBuilder = storyReporterBuilder;
        return this;
    }

    public Configuration useStepCollector(StepCollector stepCollector) {
        this.stepCollector = stepCollector;
        return this;
    }

    public Configuration useStepFinder(StepFinder stepFinder) {
        this.stepFinder = stepFinder;
        return this;
    }

    public Configuration useStepdocReporter(StepdocReporter stepdocReporter) {
        this.stepdocReporter = stepdocReporter;
        return this;
    }

    public Configuration useStepPatternParser(StepPatternParser stepPatternParser) {
        this.stepPatternParser = stepPatternParser;
        return this;
    }

    public Configuration useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
        return this;
    }

    public Configuration useParanamer(Paranamer paranamer) {
        this.paranamer = paranamer;
        return this;
    }

    public Configuration useParanamer() {
        return useParanamer(new CachingParanamer(new BytecodeReadingParanamer()));
    }

    public Configuration useParameterConverters(ParameterConverters parameterConverters) {
        this.parameterConverters = parameterConverters;
        return this;
    }

    public Configuration useViewGenerator(ViewGenerator viewGenerator) {
        this.viewGenerator = viewGenerator;
        return this;
    }

    public Configuration usePathCalculator(PathCalculator pathCalculator) {
        this.pathCalculator = pathCalculator;
        return this;
    }
}
