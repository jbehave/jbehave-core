package org.jbehave.core.configuration;

import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.configuration.UnmodifiableStoryConfiguration;
import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepdocGenerator;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnmodifiableStoryConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        StoryConfiguration delegate = new MostUsefulStoryConfiguration();
        StoryConfiguration unmodifiable = new UnmodifiableStoryConfiguration(delegate);
        assertThat(unmodifiable.keywords(), is(delegate.keywords()));
        assertThat(unmodifiable.stepCreator(), is(delegate.stepCreator()));
        assertThat(unmodifiable.storyParser(), is(delegate.storyParser()));
        assertThat(unmodifiable.storyReporter(), is(delegate.storyReporter()));
        assertThat(unmodifiable.errorStrategy(), is(delegate.errorStrategy()));
        assertThat(unmodifiable.pendingErrorStrategy(), is(delegate.pendingErrorStrategy()));
        assertThat(unmodifiable.stepdocGenerator(), is(delegate.stepdocGenerator()));
        assertThat(unmodifiable.stepdocReporter(), is(delegate.stepdocReporter()));
    }


    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException, IllegalAccessException {
        StoryConfiguration delegate = new MostUsefulStoryConfiguration();
        StoryConfiguration unmodifiable = new UnmodifiableStoryConfiguration(delegate);
        assertThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        assertThatNotAllowed(unmodifiable, "useStepCreator", StepCreator.class);
        assertThatNotAllowed(unmodifiable, "useStoryLoader", StoryLoader.class);
        assertThatNotAllowed(unmodifiable, "useStoryParser", StoryParser.class);
        assertThatNotAllowed(unmodifiable, "useStoryReporter", StoryReporter.class);
        assertThatNotAllowed(unmodifiable, "useErrorStrategy", ErrorStrategy.class);
        assertThatNotAllowed(unmodifiable, "usePendingErrorStrategy", PendingErrorStrategy.class);
        assertThatNotAllowed(unmodifiable, "useStepdocGenerator", StepdocGenerator.class);
        assertThatNotAllowed(unmodifiable, "useStepdocReporter", StepdocReporter.class);
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