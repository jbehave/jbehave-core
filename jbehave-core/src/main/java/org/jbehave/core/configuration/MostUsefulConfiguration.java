package org.jbehave.core.configuration;

import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.DefaultStepdocGenerator;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepdocGenerator;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * The configuration that works for most situations that users are likely to encounter.
 * The elements configured are:
 * <ul>
 * <li>{@link Keywords}: {@link LocalizedKeywords}</li>
 * <li>{@link StoryParser}: {@link RegexStoryParser}</li>
 * <li>{@link StoryLoader}: {@link LoadFromClasspath}</li>
 * <li>{@link FailureStrategy}: {@link RethrowingFailure}</li>
 * <li>{@link PendingStepStrategy}: {@link PassingUponPendingStep}</li>
 * <li>{@link StoryReporter}: {@link ConsoleOutput}</li>
 * <li>{@link StepCollector}: {@link MarkUnmatchedStepsAsPending}</li>
 * <li>{@link StepPatternParser}: {@link RegexPrefixCapturingPatternParser}</li>
 * <li>{@link Paranamer}: {@link NullParanamer}</li>
 * <li>{@link ParameterConverters}: {@link ParameterConverters}</li>
 * <li>{@link ViewGenerator}: {@link FreemarkerViewGenerator}</li>
 * <li>{@link StepdocGenerator}: {@link DefaultStepdocGenerator}</li>
 * <li>{@link StepdocReporter}: {@link PrintStreamStepdocReporter}</li>
 * <li>{@link EmbedderControls}: {@link EmbedderControls}</li>
 * </ul>
 */
public class MostUsefulConfiguration extends Configuration {

    public MostUsefulConfiguration() {
        useKeywords(new LocalizedKeywords());
        useStoryLoader(new LoadFromClasspath());
        useStoryParser(new RegexStoryParser(keywords()));
        useFailureStrategy(new RethrowingFailure());
        usePendingStepStrategy(new PassingUponPendingStep());
        useDefaultStoryReporter(new ConsoleOutput());
        useStepCollector(new MarkUnmatchedStepsAsPending());
        useStepPatternParser(new RegexPrefixCapturingPatternParser());
        useStepMonitor(new SilentStepMonitor());
        useParanamer(new NullParanamer());
        useParameterConverters(new ParameterConverters());
        useViewGenerator(new FreemarkerViewGenerator());
        useStepdocGenerator(new DefaultStepdocGenerator());
        useStepdocReporter(new PrintStreamStepdocReporter());
        useEmbedderControls(new EmbedderControls());
    }

}
