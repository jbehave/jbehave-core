package org.jbehave.scenario.steps;

import static org.jbehave.scenario.steps.StepType.AND;
import static org.jbehave.scenario.steps.StepType.GIVEN;
import static org.jbehave.scenario.steps.StepType.IGNORABLE;
import static org.jbehave.scenario.steps.StepType.THEN;
import static org.jbehave.scenario.steps.StepType.WHEN;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.scenario.definition.KeyWords;
import org.jbehave.scenario.i18n.I18nKeyWords;
import org.jbehave.scenario.i18n.StringEncoder;
import org.jbehave.scenario.parser.PrefixCapturingPatternBuilder;
import org.jbehave.scenario.parser.StepPatternBuilder;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * <p>
 * Class allowing steps functionality to be fully configurable, while providing
 * default values for most commonly-used cases.
 * </p>
 * <p>
 * Configuration dependencies can be provided either via constructor or via
 * setters (called use* methods to underline that a default value of the
 * dependency is always set, but can be overridden). The use methods allow to
 * override the dependencies one by one and play nicer with a Java hierarchical
 * structure, in that does allow the use of non-static member variables.
 * </p>
 */
public class StepsConfiguration {

	private StepPatternBuilder patternBuilder;
	private StepMonitor monitor;
	private Paranamer paranamer;
	private ParameterConverters parameterConverters;
	private KeyWords keywords;
	private String[] startingWords;
	private Map<StepType,String> startingWordsByType;

	public StepsConfiguration() {
		this(new I18nKeyWords());
	}

	public StepsConfiguration(KeyWords keywords) {
		this(new PrefixCapturingPatternBuilder(), new SilentStepMonitor(),
				new NullParanamer(), new ParameterConverters(), keywords);
	}

	public StepsConfiguration(ParameterConverters converters) {
		this(new PrefixCapturingPatternBuilder(), new SilentStepMonitor(),
				new NullParanamer(), converters, new I18nKeyWords());
	}

	public StepsConfiguration(StepPatternBuilder patternBuilder,
			StepMonitor monitor, Paranamer paranamer,
			ParameterConverters parameterConverters, KeyWords keywords) {
		this.patternBuilder = patternBuilder;
		this.monitor = monitor;
		this.paranamer = paranamer;
		this.parameterConverters = parameterConverters;
		this.keywords = keywords;
		startingWordsFromKeywords();
	}

    private void startingWordsFromKeywords() {
        this.startingWords = startingWordsFrom(this.keywords);
		this.startingWordsByType = startingWordsByType(this.keywords);
    }
	
	/**
	 * @deprecated Use StepsConfiguration(KeyWords)
	 */
	public StepsConfiguration(String... startingWords) {
		this(new PrefixCapturingPatternBuilder(), new SilentStepMonitor(),
				new NullParanamer(), new ParameterConverters(), startingWords);
	}
	
	public StepsConfiguration(StepPatternBuilder patternBuilder,
			StepMonitor monitor, Paranamer paranamer,
			ParameterConverters parameterConverters, String... startingWords) {
		this.patternBuilder = patternBuilder;
		this.monitor = monitor;
		this.paranamer = paranamer;
		this.parameterConverters = parameterConverters;
		this.keywords = keywordsFrom(startingWords);
		this.startingWords = startingWords;
		this.startingWordsByType = startingWordsByType(this.keywords);
	}

	/**
	 * Makes best effort to convert starting words into keywords,
	 * assuming order (GIVEN,WHEN,THEN,AND,IGNORE)
	 * 
	 * @param startingWords the array of starting words
	 * @return Keywords with given starting words values
	 */
	private KeyWords keywordsFrom(String[] startingWords) {
		Map<String, String> keywords = new HashMap<String, String>();
		if ( startingWords.length >= 5 ){
			keywords.put(KeyWords.GIVEN, startingWords[0]);
			keywords.put(KeyWords.WHEN, startingWords[1]);
			keywords.put(KeyWords.THEN, startingWords[2]);
			keywords.put(KeyWords.AND, startingWords[3]);
            keywords.put(KeyWords.IGNORABLE, startingWords[4]);
			
		}
		return new KeyWords(keywords, new StringEncoder());
	}

	protected String[] startingWordsFrom(KeyWords keywords) {
		return new String[]{keywords.given(), keywords.when(), keywords.then(), keywords.and()};
	}
		
	protected Map<StepType, String> startingWordsByType(KeyWords keywords) {
		Map<StepType, String> words = new HashMap<StepType, String>();
		words.put(GIVEN, keywords.given());
		words.put(WHEN, keywords.when());
		words.put(THEN, keywords.then());
		words.put(AND, keywords.and());
        words.put(IGNORABLE, keywords.ignorable());
		return words;
	}

	public StepPatternBuilder getPatternBuilder() {
		return patternBuilder;
	}

	public void usePatternBuilder(StepPatternBuilder patternBuilder) {
		this.patternBuilder = patternBuilder;
	}

	public StepMonitor getMonitor() {
		return monitor;
	}

	public void useMonitor(StepMonitor monitor) {
		this.monitor = monitor;
	}

	public Paranamer getParanamer() {
		return paranamer;
	}

	public void useParanamer(Paranamer paranamer) {
		this.paranamer = paranamer;
	}

	public ParameterConverters getParameterConverters() {
		return parameterConverters;
	}

	public void useParameterConverters(ParameterConverters parameterConverters) {
		this.parameterConverters = parameterConverters;
	}

	/**
	 * @deprecated Use getStartingWordsByType()
	 */
	public String[] getStartingWords() {
		return startingWords;
	}

	public Map<StepType, String> getStartingWordsByType() {
		return startingWordsByType;
	}

	public void useStartingWords(String... startingWords) {
		this.startingWords = startingWords;
	}
	
	public KeyWords getKeywords() {
		return keywords;
	}

	public void useKeyWords(KeyWords keywords) {
		this.keywords = keywords;
		startingWordsFromKeywords();
	}

}
