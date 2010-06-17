package org.jbehave.core.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.StepdocGenerator;
import org.junit.Test;

import com.thoughtworks.paranamer.Paranamer;

public class UnmodifiableConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        Configuration delegate = new MostUsefulConfiguration();
        Configuration unmodifiable = new UnmodifiableConfiguration(delegate);
        assertThat(unmodifiable.keywords(), is(delegate.keywords()));
        assertThat(unmodifiable.storyLoader(), is(delegate.storyLoader()));
        assertThat(unmodifiable.storyParser(), is(delegate.storyParser()));
        assertThat(unmodifiable.storyPathResolver(), is(delegate.storyPathResolver()));
        assertThat(unmodifiable.defaultStoryReporter(), is(delegate.defaultStoryReporter()));
        assertThat(unmodifiable.storyReporterBuilder(), is(delegate.storyReporterBuilder()));
        assertThat(unmodifiable.failureStrategy(), is(delegate.failureStrategy()));
        assertThat(unmodifiable.pendingStepStrategy(), is(delegate.pendingStepStrategy()));
        assertThat(unmodifiable.paranamer(), is(delegate.paranamer()));
        assertThat(unmodifiable.parameterConverters(), is(delegate.parameterConverters()));
        assertThat(unmodifiable.stepCollector(), is(delegate.stepCollector()));
        assertThat(unmodifiable.stepMonitor(), is(delegate.stepMonitor()));
        assertThat(unmodifiable.stepPatternParser(), is(delegate.stepPatternParser()));
        assertThat(unmodifiable.stepdocGenerator(), is(delegate.stepdocGenerator()));
        assertThat(unmodifiable.stepdocReporter(), is(delegate.stepdocReporter()));
        assertThat(unmodifiable.viewGenerator(), is(delegate.viewGenerator()));
        assertThat(unmodifiable.embedderControls(), is(delegate.embedderControls()));
    }

    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException, IllegalAccessException {
        Configuration delegate = new MostUsefulConfiguration();
        Configuration unmodifiable = new UnmodifiableConfiguration(delegate);
        assertThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        assertThatNotAllowed(unmodifiable, "useStoryLoader", StoryLoader.class);
        assertThatNotAllowed(unmodifiable, "useStoryParser", StoryParser.class);
        assertThatNotAllowed(unmodifiable, "useDefaultStoryReporter", StoryReporter.class);
        assertThatNotAllowed(unmodifiable, "useStoryReporterBuilder", StoryReporterBuilder.class);
        assertThatNotAllowed(unmodifiable, "useFailureStrategy", FailureStrategy.class);
        assertThatNotAllowed(unmodifiable, "usePendingStepStrategy", PendingStepStrategy.class);
        assertThatNotAllowed(unmodifiable, "useParanamer", Paranamer.class);
        assertThatNotAllowed(unmodifiable, "useParameterConverters", ParameterConverters.class);
        assertThatNotAllowed(unmodifiable, "useStepCollector", StepCollector.class);
        assertThatNotAllowed(unmodifiable, "useStepMonitor", StepMonitor.class);
        assertThatNotAllowed(unmodifiable, "useStepPatternParser", StepPatternParser.class);
        assertThatNotAllowed(unmodifiable, "useStepdocGenerator", StepdocGenerator.class);
        assertThatNotAllowed(unmodifiable, "useStepdocReporter", StepdocReporter.class);
        assertThatNotAllowed(unmodifiable, "useViewGenerator", ViewGenerator.class);
        assertThatNotAllowed(unmodifiable, "useEmbedderControls", EmbedderControls.class);
    }

    private void assertThatNotAllowed(Configuration unmodifiable, String methodName, Class<?> type) throws NoSuchMethodException, IllegalAccessException {
        Method method = unmodifiable.getClass().getMethod(methodName, type);
        try {
            method.invoke(unmodifiable, new Object[]{null});
        } catch (IllegalAccessException e) {
            throw e; // should not occur
        } catch (InvocationTargetException e) {
            // expected
        }
    }

}