package org.jbehave.core.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;
import org.junit.Test;

import com.thoughtworks.paranamer.Paranamer;

public class UnmodifiableStoryConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        StoryConfiguration delegate = new MostUsefulStoryConfiguration();
        StoryConfiguration unmodifiable = new UnmodifiableStoryConfiguration(delegate);
        assertThat(unmodifiable.keywords(), is(delegate.keywords()));
        assertThat(unmodifiable.stepCollector(), is(delegate.stepCollector()));
        assertThat(unmodifiable.storyParser(), is(delegate.storyParser()));
        assertThat(unmodifiable.storyReporter(), is(delegate.storyReporter()));
        assertThat(unmodifiable.failureStrategy(), is(delegate.failureStrategy()));
        assertThat(unmodifiable.pendingStepStrategy(), is(delegate.pendingStepStrategy()));
    }


    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException, IllegalAccessException {
        StoryConfiguration delegate = new MostUsefulStoryConfiguration();
        StoryConfiguration unmodifiable = new UnmodifiableStoryConfiguration(delegate);
        assertThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        assertThatNotAllowed(unmodifiable, "useStepCollector", StepCollector.class);
        assertThatNotAllowed(unmodifiable, "useStoryLoader", StoryLoader.class);
        assertThatNotAllowed(unmodifiable, "useStoryParser", StoryParser.class);
        assertThatNotAllowed(unmodifiable, "useStoryReporter", StoryReporter.class);
        assertThatNotAllowed(unmodifiable, "useErrorStrategy", FailureStrategy.class);
        assertThatNotAllowed(unmodifiable, "usePendingStepStrategy", PendingStepStrategy.class);
        assertThatNotAllowed(unmodifiable, "useParanamer", Paranamer.class);
        assertThatNotAllowed(unmodifiable, "useParameterConverters", ParameterConverters.class);
        assertThatNotAllowed(unmodifiable, "useStepMonitor", StepMonitor.class);
        assertThatNotAllowed(unmodifiable, "useStepPatternParser", StepPatternParser.class);
    }

    private void assertThatNotAllowed(StoryConfiguration unmodifiable, String methodName, Class<?> type) throws NoSuchMethodException, IllegalAccessException {
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