package org.jbehave.core.steps;

import com.thoughtworks.paranamer.NullParanamer;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;

import java.util.Locale;

/**
 * The configuration that works for most situations that users are likely to encounter.
 * The elements configured are:
 * <ul>
 * <li>{@link KeyWords}: new I18nKeyWords(Locale.ENGLISH)</li>
 * <li>{@link StepPatternParser}: new RegexPrefixCapturingPatternParser("$")</li>
 * <li>{@link StepMonitor}: new ClasspathStoryDefiner(new PatternStoryParser(keywords()))</li>
 * <li>{@link Paranamer}: new NullParanamer()</li>
 * <li>{@link ParameterConverters}: new ParameterConverters()</li>    
 * </ul>
 */
public class MostUsefulStepsConfiguration extends StepsConfiguration {

    public MostUsefulStepsConfiguration() {
        useKeywords(new LocalizedKeywords(Locale.ENGLISH));
        usePatternParser(new RegexPrefixCapturingPatternParser("$"));
        useMonitor(new SilentStepMonitor());
        useParanamer(new NullParanamer());
        useParameterConverters(new ParameterConverters());
    }

}
