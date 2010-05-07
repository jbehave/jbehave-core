package org.jbehave.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parser.LoadFromClasspath;
import org.jbehave.core.parser.RegexStoryParser;
import org.jbehave.core.parser.StoryLoader;
import org.jbehave.core.parser.StoryParser;
import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.DefaultStepdocGenerator;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepdocGenerator;

/**
 * <p>
 * Provides the story configuration used by the {@link StoryRunner} and the
 * in the {@link RunnableStory} implementations to customise its runtime properties.
 * </p>
 * <p>
 * StoryConfiguration dependencies can be provided either via constructor or via
 * use* methods, which override the the default values of the
 * dependency, which is always set. The use methods allow to
 * override the dependencies one by one and play nicer with a Java hierarchical
 * structure, in that does allow the use of non-static member variables.
 * </p>
 *
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public class StoryConfiguration {

    /**
     * Use English language for keywords
     */
    private Keywords keywords = new LocalizedKeywords(Locale.ENGLISH);
    /**
     * Provides pending steps where unmatched steps exist.
     */
    private StepCreator stepCreator = new MarkUnmatchedStepsAsPending();
    /**
     * Parses the textual representation via pattern matching of keywords
     */
    private StoryParser storyParser = new RegexStoryParser(keywords);
    /**
     * Loads story content from classpath
     */
    private StoryLoader storyLoader = new LoadFromClasspath();
    /**
     * Resolves story paths from class names using underscored camel case with ".story" extension
     */
    private StoryPathResolver storyPathResolver = new UnderscoredCamelCaseResolver(".story");
    /**
     * Handles errors by re-throwing them.
     * <p/>
     * If there are multiple scenarios in a single story, this could
     * cause the story to stop after the first failing scenario.
     * <p/>
     * Users wanting a different behaviour may use
     * {@link org.jbehave.core.errors.ErrorStrategyInWhichWeTrustTheReporter}.
     */
    private ErrorStrategy errorStrategy = ErrorStrategy.RETHROW;
    /**
     * Allows pending steps to pass, so that steps that to do not match any method will not
     * cause failure.
     * <p/>
     * Uses wanting a stricter behaviour for pending steps may use
     * {@link org.jbehave.core.errors.PendingErrorStrategy.FAILING}.
     */
    private PendingErrorStrategy pendingErrorStrategy = PendingErrorStrategy.PASSING;
    /**
     * Reports stories to console output
     */
    private StoryReporter storyReporter = new ConsoleOutput();
    /**
     * Collects story reporters by story path 
     */
    private Map<String, StoryReporter> storyReporters = new HashMap<String,  StoryReporter>();
    /**
     * Use default stepdoc generator
     */
    private StepdocGenerator stepdocGenerator = new DefaultStepdocGenerator();
    /**
     * Reports stepdocs to System.out, while reporting methods
     */
    private StepdocReporter stepdocReporter = new PrintStreamStepdocReporter(System.out, true);

    /**
     * Default no-op constructor, uses the default instances defined for member variables.
     */
    public StoryConfiguration() {
    }

    /**
     * Constructor that allows all dependencies to be injected
     *
     * @param keywords
     * @param stepCreator
     * @param storyParser
     * @param storyLoader
     * @param storyPathResolver
     * @param errorStrategy
     * @param stepdocReporter
     * @param stepdocGenerator
     * @param storyReporter
     * @param pendingErrorStrategy
     */
    protected StoryConfiguration(Keywords keywords, StepCreator stepCreator, StoryParser storyParser, StoryLoader storyLoader, StoryPathResolver storyPathResolver, ErrorStrategy errorStrategy, StepdocReporter stepdocReporter, StepdocGenerator stepdocGenerator, StoryReporter storyReporter, PendingErrorStrategy pendingErrorStrategy) {
        this.keywords = keywords;
        this.stepCreator = stepCreator;
        this.storyParser = storyParser;
        this.storyLoader = storyLoader;
        this.storyPathResolver = storyPathResolver;
        this.errorStrategy = errorStrategy;
        this.stepdocReporter = stepdocReporter;
        this.stepdocGenerator = stepdocGenerator;
        this.storyReporter = storyReporter;
        this.pendingErrorStrategy = pendingErrorStrategy;
    }


    public StepCreator stepCreator() {
        return stepCreator;
    }

    public StoryParser storyParser() {
        return storyParser;
    }

    public StoryLoader storyLoader(){
        return storyLoader;
    }
    
    public StoryPathResolver storyPathResolver(){
        return storyPathResolver;
    }

    public ErrorStrategy errorStrategy() {
        return errorStrategy;
    }

    public PendingErrorStrategy pendingErrorStrategy() {
        return pendingErrorStrategy;
    }

    public StoryReporter storyReporter() {
        return storyReporter;
    }

    public StoryReporter storyReporter(String storyPath) {
        StoryReporter storyReporter = storyReporters.get(storyPath);
        if (storyReporter != null ){
            return storyReporter;
        }
        // default to configured story reporter
        // TODO consider merging the two methods
        return storyReporter();
    }


    public Keywords keywords() {
        return keywords;
    }

    public StepdocGenerator stepdocGenerator() {
        return stepdocGenerator;
    }

    public StepdocReporter stepdocReporter() {
        return stepdocReporter;
    }

    public StoryConfiguration useKeywords(Keywords keywords) {
        this.keywords = keywords;
        return this;
    }

    public StoryConfiguration useStepCreator(StepCreator stepCreator) {
        this.stepCreator = stepCreator;
        return this;
    }

    public StoryConfiguration usePendingErrorStrategy(PendingErrorStrategy pendingErrorStrategy) {
        this.pendingErrorStrategy = pendingErrorStrategy;
        return this;
    }

    public StoryConfiguration useErrorStrategy(ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
        return this;
    }

    public StoryConfiguration useStoryParser(StoryParser storyParser) {
        this.storyParser = storyParser;
        return this;
    }

    public StoryConfiguration useStoryLoader(StoryLoader storyLoader){
        this.storyLoader = storyLoader;
        return this;
    }

    public StoryConfiguration useStoryPathResolver(StoryPathResolver storyPathResolver) {
        this.storyPathResolver = storyPathResolver;
        return this;
    }

    public StoryConfiguration useStoryReporter(StoryReporter storyReporter) {
        this.storyReporter = storyReporter;
        return this;
    }
    
    public StoryConfiguration useStoryReporter(String storyPath, StoryReporter storyReporter){
        this.storyReporters.put(storyPath, storyReporter);
        return this;
    }

	public StoryConfiguration useStoryReporters(Map<String, StoryReporter> storyReporters) {
		this.storyReporters.putAll(storyReporters);
        return this;
	}

    public StoryConfiguration useStepdocReporter(StepdocReporter stepdocReporter) {
        this.stepdocReporter = stepdocReporter;
        return this;
    }

    public StoryConfiguration useStepdocGenerator(StepdocGenerator stepdocGenerator) {
        this.stepdocGenerator = stepdocGenerator;
        return this;
    }
}
