package org.jbehave.core.configuration;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

import org.jbehave.core.Embeddable;
import org.jbehave.core.condition.ReflectionBasedStepConditionMatcher;
import org.jbehave.core.condition.StepConditionMatcher;
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
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.AliasParser;
import org.jbehave.core.parsers.CompositeParser;
import org.jbehave.core.parsers.JsonAliasParser;
import org.jbehave.core.parsers.RegexCompositeParser;
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
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.context.StepsContext;

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
    protected StoryControls storyControls;

    /**
     * Use English language for keywords
     */
    protected Keywords keywords;

    /**
     * Provides pending steps where unmatched steps exist.
     */
    protected StepCollector stepCollector;

    /**
     * Parses the textual representation via pattern matching of keywords
     */
    protected StoryParser storyParser;

    /**
     * Parses composite steps from their textual representation
     */
    protected CompositeParser compositeParser;

    /**
     * Loads story content from classpath
     */
    protected StoryLoader storyLoader;

    /**
     * Parse aliases from resources
     */
    protected AliasParser aliasParser;

    /**
     * Resolves story paths from class names using underscored camel case with
     * ".story" extension
     */
    protected StoryPathResolver storyPathResolver;

    /**
     * Handles errors by re-throwing them.
     * <p/>
     * If there are multiple scenarios in a single story, this could cause the
     * story to stop after the first failing scenario.
     * <p/>
     * Users wanting a different behaviour may use
     * {@link SilentlyAbsorbingFailure}.
     */
    protected FailureStrategy failureStrategy;

    /**
     * Allows pending steps to pass, so that steps that to do not match any
     * method will not cause failure.
     * <p/>
     * Uses wanting a stricter behaviour for pending steps may use
     * {@link FailingUponPendingStep}.
     */
    protected PendingStepStrategy pendingStepStrategy;

    /**
     * Reports stories to console output
     */
    protected StoryReporter defaultStoryReporter;

    /**
     * The story reporter builder
     */
    protected StoryReporterBuilder storyReporterBuilder;

    /**
     * The steps context
     */
    protected StepsContext stepsContext;

    /**
     * Finder of matching candidate steps
     */
    protected StepFinder stepFinder;

    /**
     * Report candidate steps found to a PrintStream
     */
    protected StepdocReporter stepdocReporter;

    /**
     * Pattern build that uses prefix for identifying parameters
     */
    protected StepPatternParser stepPatternParser;

    /**
     * Controls of step parameterization
     */
    protected ParameterControls parameterControls;

    /**
     * Silent monitoring that does not produce any noise of the step matching.
     * </p> If needed, users can switch on verbose monitoring using
     * {@link PrintStreamStepMonitor}
     */
    protected StepMonitor stepMonitor;

    /**
     * Paranamer is switched off by default
     */
    protected Paranamer paranamer;

    /**
     * Use default built-in parameter converters
     */
    protected ParameterConverters parameterConverters;

    /**
     * Use default built-in ExamplesTable parsers
     */
    protected TableParsers tableParsers;

    /**
     * Use default built-in ExamplesTable transformers
     */
    protected TableTransformers tableTransformers;

    /**
     * Use Freemarker-based view generator
     */
    protected ViewGenerator viewGenerator;

    /**
     * Use an absolute path calculator
     */
    protected PathCalculator pathCalculator;

    /**
     * Paths to resources containing composite steps definitions
     */
    protected Set<String> compositePaths;

    /**
     * Paths to resources containing ailas definitions
     */
    protected Set<String> aliasPaths;

    /**
     * The examples table factory
     */
    protected ExamplesTableFactory examplesTableFactory;

    /**
     * The story execution comparator
     */
    protected Comparator<Story> storyExecutionComparator;

    /**
     * Enables parallelization of story level examples
     */
    private boolean parallelStoryExamplesEnabled;

    /**
     * The step condition matcher to match conditional steps
     */
    protected StepConditionMatcher stepConditionMatcher;

    public Configuration() {
    }

    public Keywords keywords() {
        if (keywords == null) {
            keywords = new LocalizedKeywords();
        }
        return keywords;
    }

    public boolean dryRun() {
        return storyControls().dryRun();
    }

    public StoryControls storyControls() {
        if (storyControls == null) {
            storyControls = new StoryControls();
        }
        return storyControls;
    }

    public StoryParser storyParser() {
        if (storyParser == null) {
            storyParser = new RegexStoryParser(examplesTableFactory());
        }
        return storyParser;
    }

    public CompositeParser compositeParser() {
        if (compositeParser == null) {
            compositeParser = new RegexCompositeParser(keywords());
        }
        return compositeParser;
    }

    public StoryLoader storyLoader() {
        if (storyLoader == null) {
            storyLoader = new LoadFromClasspath();
        }
        return storyLoader;
    }

    public AliasParser aliasParser() {
        if (aliasParser == null) {
            aliasParser = new JsonAliasParser(keywords());
        }
        return aliasParser;
    }

    public Comparator<Story> storyExecutionComparator() {
        if (storyExecutionComparator == null) {
            storyExecutionComparator = Comparator.comparing(Story::getPath, Comparator.naturalOrder());
        }
        return storyExecutionComparator;
    }

    public ExamplesTableFactory examplesTableFactory() {
        if (examplesTableFactory == null) {
            examplesTableFactory = new ExamplesTableFactory(keywords(), storyLoader(), parameterConverters(),
                    parameterControls(), tableParsers(), tableTransformers());
        }
        return examplesTableFactory;
    }

    public StoryPathResolver storyPathResolver() {
        if (storyPathResolver == null) {
            storyPathResolver = new UnderscoredCamelCaseResolver();
        }
        return storyPathResolver;
    }

    public FailureStrategy failureStrategy() {
        if (failureStrategy == null) {
            failureStrategy = new RethrowingFailure();
        }
        return failureStrategy;
    }

    public PendingStepStrategy pendingStepStrategy() {
        if (pendingStepStrategy == null) {
            pendingStepStrategy = new PassingUponPendingStep();
        }
        return pendingStepStrategy;
    }

    public StoryReporter defaultStoryReporter() {
        if (defaultStoryReporter == null) {
            defaultStoryReporter = new ConsoleOutput();
        }
        return defaultStoryReporter;
    }

    public StoryReporter storyReporter(String storyPath) {
        return storyReporterBuilder().build(storyPath);
    }

    public StoryReporterBuilder storyReporterBuilder() {
        if (storyReporterBuilder == null) {
            storyReporterBuilder = new StoryReporterBuilder();
        }
        return storyReporterBuilder;
    }

    public StepsContext stepsContext() {
        if (stepsContext == null) {
            stepsContext = new StepsContext();
        }
        return stepsContext;
    }

    public StepCollector stepCollector() {
        if (stepCollector == null) {
            stepCollector = new MarkUnmatchedStepsAsPending(stepFinder(), keywords());
        }
        return stepCollector;
    }

    public StepFinder stepFinder() {
        if (stepFinder == null) {
            stepFinder = new StepFinder(stepConditionMatcher());
        }
        return stepFinder;
    }

    public StepdocReporter stepdocReporter() {
        if (stepdocReporter == null) {
            stepdocReporter = new PrintStreamStepdocReporter();
        }
        return stepdocReporter;
    }

    public StepPatternParser stepPatternParser() {
        if (stepPatternParser == null) {
            stepPatternParser = new RegexPrefixCapturingPatternParser();
        }
        return stepPatternParser;
    }

    public ParameterControls parameterControls() {
        if (parameterControls == null) {
            parameterControls = new ParameterControls();
        }
        return parameterControls;
    }

    public StepMonitor stepMonitor() {
        if (stepMonitor == null) {
            stepMonitor = new SilentStepMonitor();
        }
        return stepMonitor;
    }

    public Paranamer paranamer() {
        if (paranamer == null) {
            paranamer = new NullParanamer();
        }
        return paranamer;
    }

    public ParameterConverters parameterConverters() {
        if (parameterConverters == null) {
            parameterConverters = new ParameterConverters(stepMonitor(), keywords(), storyLoader(), parameterControls(),
                    tableTransformers());
        }
        return parameterConverters;
    }

    public TableParsers tableParsers() {
        if (tableParsers == null) {
            tableParsers = new TableParsers(keywords(), parameterConverters(), Optional.empty());
        }
        return tableParsers;
    }

    public TableTransformers tableTransformers() {
        if (tableTransformers == null) {
            tableTransformers = new TableTransformers();
        }
        return tableTransformers;
    }

    public ViewGenerator viewGenerator() {
        if (viewGenerator == null) {
            viewGenerator = new FreemarkerViewGenerator();
        }
        return viewGenerator;
    }

    public PathCalculator pathCalculator() {
        if (pathCalculator == null) {
            pathCalculator = new AbsolutePathCalculator();
        }
        return pathCalculator;
    }

    public Set<String> compositePaths() {
        if (compositePaths == null) {
            compositePaths = Collections.emptySet();
        }
        return compositePaths;
    }

    public Set<String> aliasPaths() {
        if (aliasPaths == null) {
            aliasPaths = Collections.emptySet();
        }
        return aliasPaths;
    }

    public StepConditionMatcher stepConditionMatcher() {
        if (stepConditionMatcher == null) {
            stepConditionMatcher = new ReflectionBasedStepConditionMatcher();
        }
        return stepConditionMatcher;
    }

    public Configuration useStepConditionMatcher(StepConditionMatcher stepConditionMatcher) {
        this.stepConditionMatcher = stepConditionMatcher;
        return this;
    }

    public Configuration useKeywords(Keywords keywords) {
        this.keywords = keywords;
        return this;
    }

    public Configuration doDryRun(Boolean dryRun) {
        this.storyControls().doDryRun(dryRun);
        return this;
    }

    public Configuration useStoryControls(StoryControls storyControls) {
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

    public Configuration useCompositeParser(CompositeParser compositeParser) {
        this.compositeParser = compositeParser;
        return this;
    }

    public Configuration useStoryLoader(StoryLoader storyLoader) {
        this.storyLoader = storyLoader;
        return this;
    }

    public Configuration useAliasParser(AliasParser aliasParser) {
        this.aliasParser = aliasParser;
        return this;
    }

    public Configuration useExamplesTableFactory(ExamplesTableFactory examplesTableFactory) {
        this.examplesTableFactory = examplesTableFactory;
        return this;
    }

    public Configuration useStoryExecutionComparator(Comparator<Story> storyExecutionComparator) {
        this.storyExecutionComparator = storyExecutionComparator;
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

    public Configuration useParameterControls(ParameterControls parameterControls) {
        this.parameterControls = parameterControls;
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

    public Configuration useParameterConverters(ParameterConverters parameterConverters) {
        this.parameterConverters = parameterConverters;
        return this;
    }

    public Configuration useTableTransformers(TableTransformers tableTransformers) {
        this.tableTransformers = tableTransformers;
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

    public Configuration useCompositePaths(Set<String> compositePaths) {
        this.compositePaths = compositePaths;
        return this;
    }

    public Configuration useAliasPaths(Set<String> aliasPaths) {
        this.aliasPaths = aliasPaths;
        return this;
    }

    public boolean isParallelStoryExamplesEnabled() {
        return parallelStoryExamplesEnabled;
    }

    public void setParallelStoryExamplesEnabled(boolean parallelStoryExamplesEnabled) {
        this.parallelStoryExamplesEnabled = parallelStoryExamplesEnabled;
    }

    public Configuration useStepsContext(StepsContext stepsContext) {
        this.stepsContext = stepsContext;
        return this;
    }
}
