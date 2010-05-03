package org.jbehave.core;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parser.*;
import org.jbehave.core.reporters.*;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.DefaultStepdocGenerator;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepdocGenerator;

import java.util.Locale;

/**
 * The configuration that works for most situations that users are likely to encounter.
 * The elements configured are:
 * <ul>
 * <li>{@link org.jbehave.core.model.Keywords}: new I18nKeyWords()</li>
 * <li>{@link StepCreator}: new UnmatchedToPendingStepCreator()</li>
 * <li>{@link StoryParser}: new PatternStoryParser(keywords())</li>
 * <li>{@link org.jbehave.core.parser.StoryLoader}: new ClasspathLoader()</li>
 * <li>{@link ErrorStrategy}: ErrorStrategy.RETHROW</li>
 * <li>{@link PendingErrorStrategy}: PendingErrorStrategy.PASSING</li>
 * <li>{@link StoryReporter}: new PassSilentlyDecorator(new PrintStreamStoryReporter())</li>
 * <li>{@link StepdocGenerator}: new DefaultStepdocGenerator()</li>
 * <li>{@link StepdocReporter}: new PrintStreamStepdocReporter(true)</li>
 * </ul>
 */
public class MostUsefulStoryConfiguration extends StoryConfiguration {

    public MostUsefulStoryConfiguration() {
        useKeywords(new LocalizedKeywords(Locale.ENGLISH));
        useStepCreator(new MarkUnmatchedStepsAsPending());
        useStoryParser(new RegexStoryParser(keywords()));
        useStoryLoader(new LoadFromClasspath());
        useErrorStrategy(ErrorStrategy.RETHROW);
        usePendingErrorStrategy(PendingErrorStrategy.PASSING);
        useStoryReporter(new SilentSuccessFilter(new PrintStreamOutput()));
        useStepdocReporter(new PrintStreamStepdocReporter(true));
        useStepdocGenerator(new DefaultStepdocGenerator());
    }

}
