package org.jbehave.core.steps;

import static org.jbehave.core.steps.StepType.AND;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.IGNORABLE;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jbehave.core.model.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parser.PrefixCapturingPatternBuilder;
import org.jbehave.core.parser.StepPatternBuilder;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * <p>
 * Provides the steps configuration used by the {@link org.jbehave.core.steps.StepsFactory} and the
 * in the {@link org.jbehave.core.steps.Steps} implementations to customise its runtime properties.
 * </p>
 * <p>
 * StepsConfiguration dependencies can be provided either via constructor or via
 * use* methods, which override the the default values of the
 * dependency, which is always set. The use methods allow to
 * override the dependencies one by one and play nicer with a Java hierarchical
 * structure, in that does allow the use of non-static member variables.
 * </p>
 */
public abstract class StepsConfiguration {

    /**
     * Use English keywords for step matching
     */
    private Keywords keywords = new LocalizedKeywords(Locale.ENGLISH);
    /**
     * Pattern build that uses prefix for identifying parameters 
     */
    private StepPatternBuilder patternBuilder = new PrefixCapturingPatternBuilder("$");
    /**
     * Silent monitoring that does not produce any noise of the step matching.
     * </p>
     * If needed, users can switch on verbose monitoring using {@link PrintStreamStepMonitor}
     */
    private StepMonitor monitor = new SilentStepMonitor();
    /**
     * Paranamer use is switched off by default
     */
    private Paranamer paranamer = new NullParanamer();
    /**
     * Use default built-in parameter converters
     */
    private ParameterConverters parameterConverters = new ParameterConverters();
    /**
     * Map of starting words, keyed on step type, as derived from the keywords provided
     */
    private Map<StepType, String> startingWordsByType = startingWordsByType(this.keywords);

    /**
     * Default no-op constructor, uses the default instances defined for member variables.
     */
    protected StepsConfiguration() {

    }

    /**
     * Constructor that allows all dependencies to be injected
     *
     * @param keywords
     * @param patternBuilder
     * @param monitor
     * @param paranamer
     * @param parameterConverters
     */
    protected StepsConfiguration(Keywords keywords, StepPatternBuilder patternBuilder,
                              StepMonitor monitor, Paranamer paranamer,
                              ParameterConverters parameterConverters) {
        this.keywords = keywords;
        this.patternBuilder = patternBuilder;
        this.monitor = monitor;
        this.paranamer = paranamer;
        this.parameterConverters = parameterConverters;
        this.startingWordsByType = startingWordsByType(this.keywords);
    }

    protected Map<StepType, String> startingWordsByType(Keywords keywords) {
        Map<StepType, String> words = new HashMap<StepType, String>();
        words.put(GIVEN, keywords.given());
        words.put(WHEN, keywords.when());
        words.put(THEN, keywords.then());
        words.put(AND, keywords.and());
        words.put(IGNORABLE, keywords.ignorable());
        return words;
    }

    public StepPatternBuilder patternBuilder() {
        return patternBuilder;
    }

    public StepsConfiguration usePatternBuilder(StepPatternBuilder patternBuilder) {
        this.patternBuilder = patternBuilder;
        return this;
    }

    public StepMonitor monitor() {
        return monitor;
    }

    public StepsConfiguration useMonitor(StepMonitor monitor) {
        this.monitor = monitor;
        return this;
    }

    public Paranamer paranamer() {
        return paranamer;
    }

    public StepsConfiguration useParanamer(Paranamer paranamer) {
        this.paranamer = paranamer;
        return this;
    }

    public ParameterConverters parameterConverters() {
        return parameterConverters;
    }

    public StepsConfiguration useParameterConverters(ParameterConverters parameterConverters) {
        this.parameterConverters = parameterConverters;
        return this;
    }

    public Map<StepType, String> getStartingWordsByType() {
        return startingWordsByType;
    }

    public Keywords keywords() {
        return keywords;
    }

    public StepsConfiguration useKeywords(Keywords keywords) {
        this.keywords = keywords;
        this.startingWordsByType = startingWordsByType(this.keywords);
        return this;
    }

}
