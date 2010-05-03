package org.jbehave.core;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parser.StoryLoader;
import org.jbehave.core.parser.StoryParser;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepdocGenerator;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.jbehave.Ensure.ensureThat;

public class UnmodifiableStoryConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        StoryConfiguration delegate = new MostUsefulStoryConfiguration();
        StoryConfiguration unmodifiable = new UnmodifiableStoryConfiguration(delegate);
        ensureThat(unmodifiable.keywords(), is(delegate.keywords()));
        ensureThat(unmodifiable.stepCreator(), is(delegate.stepCreator()));
        ensureThat(unmodifiable.storyParser(), is(delegate.storyParser()));
        ensureThat(unmodifiable.storyReporter(), is(delegate.storyReporter()));
        ensureThat(unmodifiable.errorStrategy(), is(delegate.errorStrategy()));
        ensureThat(unmodifiable.pendingErrorStrategy(), is(delegate.pendingErrorStrategy()));
        ensureThat(unmodifiable.stepdocGenerator(), is(delegate.stepdocGenerator()));
        ensureThat(unmodifiable.stepdocReporter(), is(delegate.stepdocReporter()));
    }


    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException, IllegalAccessException {
        StoryConfiguration delegate = new MostUsefulStoryConfiguration();
        StoryConfiguration unmodifiable = new UnmodifiableStoryConfiguration(delegate);
        ensureThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        ensureThatNotAllowed(unmodifiable, "useStepCreator", StepCreator.class);
        ensureThatNotAllowed(unmodifiable, "useStoryLoader", StoryLoader.class);
        ensureThatNotAllowed(unmodifiable, "useStoryParser", StoryParser.class);
        ensureThatNotAllowed(unmodifiable, "useStoryReporter", StoryReporter.class);
        ensureThatNotAllowed(unmodifiable, "useErrorStrategy", ErrorStrategy.class);
        ensureThatNotAllowed(unmodifiable, "usePendingErrorStrategy", PendingErrorStrategy.class);
        ensureThatNotAllowed(unmodifiable, "useStepdocGenerator", StepdocGenerator.class);
        ensureThatNotAllowed(unmodifiable, "useStepdocReporter", StepdocReporter.class);
    }

    private void ensureThatNotAllowed(StoryConfiguration unmodifiable, String methodName, Class<?> type) throws NoSuchMethodException, IllegalAccessException {
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