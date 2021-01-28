package org.jbehave.core.annotations;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.*;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.*;
import org.jbehave.core.steps.*;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
@Documented
public @interface Configure {

    Class<? extends Configuration> using() default MostUsefulConfiguration.class;

    Class<? extends Keywords> keywords() default LocalizedKeywords.class;

    Class<? extends StoryControls> storyControls() default StoryControls.class;

    Class<? extends StepCollector> stepCollector() default MarkUnmatchedStepsAsPending.class;

    Class<? extends StoryParser> storyParser() default RegexStoryParser.class;

    Class<? extends StoryLoader> storyLoader() default LoadFromClasspath.class;

    Class<? extends StoryPathResolver> storyPathResolver() default UnderscoredCamelCaseResolver.class;

    Class<? extends StepdocReporter> stepdocReporter() default PrintStreamStepdocReporter.class;

    Class<? extends FailureStrategy> failureStrategy() default RethrowingFailure.class;

    Class<? extends PendingStepStrategy> pendingStepStrategy() default PassingUponPendingStep.class;

    Class<? extends StepPatternParser> stepPatternParser() default RegexPrefixCapturingPatternParser.class;

    Class<? extends StepFinder> stepFinder() default StepFinder.class;

    Class<? extends StepMonitor> stepMonitor() default SilentStepMonitor.class;

    Class<? extends ParameterConverter>[] parameterConverters() default {};

    boolean inheritParameterConverters() default true;

    Class<? extends ParameterControls> parameterControls() default ParameterControls.class;

    Class<? extends Paranamer> paranamer() default NullParanamer.class;

    Class<? extends EmbedderControls> embedderControls() default EmbedderControls.class;

    Class<? extends ViewGenerator> viewGenerator() default FreemarkerViewGenerator.class;

    Class<? extends TableTransformers> tableTransformers() default TableTransformers.class;

    Class<? extends PathCalculator> pathCalculator() default AbsolutePathCalculator.class;

    Class<? extends StoryReporterBuilder> storyReporterBuilder() default StoryReporterBuilder.class;

    Class<? extends StoryReporter> defaultStoryReporter() default ConsoleOutput.class;

}
