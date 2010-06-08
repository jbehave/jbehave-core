package org.jbehave.core.configuration;

import java.util.Locale;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.SilentSuccessFilter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepCollector;

/**
 * The configuration that works for most situations that users are likely to encounter.
 * The elements configured are:
 * <ul>
 * <li>{@link Keywords}: {@link LocalizedKeywords}</li>
 * <li>{@link StepCollector}: {@link MarkUnmatchedStepsAsPending}</li>
 * <li>{@link StoryParser}: {@link RegexStoryParser}</li>
 * <li>{@link StoryLoader}: {@link LoadFromClasspath}</li>
 * <li>{@link ErrorStrategy}: {@link ErrorStrategy.RETHROW}</li>
 * <li>{@link PendingErrorStrategy}: {@link PendingErrorStrategy.PASSING}</li>
 * <li>{@link StoryReporter}: {@link ConsoleOutput}</li>
 * </ul>
 */
public class MostUsefulStoryConfiguration extends StoryConfiguration {

    public MostUsefulStoryConfiguration() {
        useKeywords(new LocalizedKeywords(Locale.ENGLISH));
        useStepCollector(new MarkUnmatchedStepsAsPending());
        useStoryParser(new RegexStoryParser(keywords()));
        useStoryLoader(new LoadFromClasspath());
        useErrorStrategy(ErrorStrategy.RETHROW);
        usePendingErrorStrategy(PendingErrorStrategy.PASSING);
        useStoryReporter(new SilentSuccessFilter(new ConsoleOutput()));
    }

}
