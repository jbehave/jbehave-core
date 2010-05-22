package org.jbehave.core.configuration;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.parsers.*;
import org.jbehave.core.reporters.*;
import org.jbehave.core.steps.DefaultStepdocGenerator;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepdocGenerator;

import java.util.Locale;

/**
 * The configuration that works for most situations that users are likely to encounter.
 * The elements configured are:
 * <ul>
 * <li>{@link org.jbehave.core.model.Keywords}: {@link LocalizedKeywords}</li>
 * <li>{@link StepCreator}: {@link MarkUnmatchedStepsAsPending}</li>
 * <li>{@link StoryParser}: {@link RegexStoryParser}</li>
 * <li>{@link StoryLoader}: {@link LoadFromClasspath}</li>
 * <li>{@link ErrorStrategy}: {@link ErrorStrategy.RETHROW}</li>
 * <li>{@link PendingErrorStrategy}: {@link PendingErrorStrategy.PASSING}</li>
 * <li>{@link StoryReporter}: {@link ConsoleOutput}</li>
 * <li>{@link StepdocGenerator}: {@link DefaultStepdocGenerator}</li>
 * <li>{@link StepdocReporter}: {@link PrintStreamStepdocReporter}</li>
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
