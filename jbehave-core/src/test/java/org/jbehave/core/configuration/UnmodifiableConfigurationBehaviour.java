package org.jbehave.core.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.hamcrest.Matchers;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;
import org.junit.Test;

import com.thoughtworks.paranamer.Paranamer;

import static org.hamcrest.Matchers.is;

import static org.hamcrest.MatcherAssert.assertThat;

public class UnmodifiableConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        Configuration delegate = new MostUsefulConfiguration();
        String storyPath = "path";
        Configuration unmodifiable = new UnmodifiableConfiguration(delegate);
        assertThat(unmodifiable.dryRun(), is(delegate.dryRun()));
        assertThat(unmodifiable.keywords(), is(delegate.keywords()));
        assertThat(unmodifiable.storyControls(), is(delegate.storyControls()));
        assertThat(unmodifiable.storyLoader(), is(delegate.storyLoader()));
        assertThat(unmodifiable.storyParser(), is(delegate.storyParser()));
        assertThat(unmodifiable.storyPathResolver(), is(delegate.storyPathResolver()));
        assertThat(unmodifiable.defaultStoryReporter(), is(delegate.defaultStoryReporter()));
        assertThat(unmodifiable.storyReporter(storyPath), is(Matchers.<Object>notNullValue(Object.class)));
        assertThat(unmodifiable.storyReporterBuilder(), is(delegate.storyReporterBuilder()));
        assertThat(unmodifiable.failureStrategy(), is(delegate.failureStrategy()));
        assertThat(unmodifiable.pendingStepStrategy(), is(delegate.pendingStepStrategy()));
        assertThat(unmodifiable.paranamer(), is(delegate.paranamer()));
        assertThat(unmodifiable.parameterConverters(), is(delegate.parameterConverters()));
        assertThat(unmodifiable.stepCollector(), is(delegate.stepCollector()));
        assertThat(unmodifiable.stepMonitor(), is(delegate.stepMonitor()));
        assertThat(unmodifiable.stepPatternParser(), is(delegate.stepPatternParser()));
        assertThat(unmodifiable.viewGenerator(), is(delegate.viewGenerator()));
    }

    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException,
            IllegalAccessException {
        Configuration delegate = new MostUsefulConfiguration();
        Configuration unmodifiable = new UnmodifiableConfiguration(delegate);
        assertThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        assertThatNotAllowed(unmodifiable, "doDryRun", Boolean.class);
        assertThatNotAllowed(unmodifiable, "useStoryControls", StoryControls.class);
        assertThatNotAllowed(unmodifiable, "useStoryLoader", StoryLoader.class);
        assertThatNotAllowed(unmodifiable, "useStoryParser", StoryParser.class);
        assertThatNotAllowed(unmodifiable, "useDefaultStoryReporter", StoryReporter.class);
        assertThatNotAllowed(unmodifiable, "useStoryReporterBuilder", StoryReporterBuilder.class);
        assertThatNotAllowed(unmodifiable, "useStoryPathResolver", StoryPathResolver.class);
        assertThatNotAllowed(unmodifiable, "useFailureStrategy", FailureStrategy.class);
        assertThatNotAllowed(unmodifiable, "usePendingStepStrategy", PendingStepStrategy.class);
        assertThatNotAllowed(unmodifiable, "useParanamer", Paranamer.class);
        assertThatNotAllowed(unmodifiable, "useParameterConverters", ParameterConverters.class);
        assertThatNotAllowed(unmodifiable, "useStepCollector", StepCollector.class);
        assertThatNotAllowed(unmodifiable, "useStepMonitor", StepMonitor.class);
        assertThatNotAllowed(unmodifiable, "useStepPatternParser", StepPatternParser.class);
        assertThatNotAllowed(unmodifiable, "useViewGenerator", ViewGenerator.class);
        assertThatNotAllowed(unmodifiable, "useStoryPathResolver", StoryPathResolver.class);
    }

    private void assertThatNotAllowed(Configuration unmodifiable, String methodName, Class<?>... types)
            throws NoSuchMethodException, IllegalAccessException {
        Method method = unmodifiable.getClass().getMethod(methodName, types);
        try {
            method.invoke(unmodifiable, nullArgsFor(types));
        } catch (IllegalAccessException e) {
            throw e; // should not occur
        } catch (InvocationTargetException e) {
            // expected
        }
    }

    private Object[] nullArgsFor(Class<?>[] types) {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = null;
        }
        return args;
    }

    @Test
    public void shouldReportDelegateInToString() {
        assertThat(new UnmodifiableConfiguration(new MostUsefulConfiguration()).toString(), Matchers
                .containsString(MostUsefulConfiguration.class.getName()));
    }
}