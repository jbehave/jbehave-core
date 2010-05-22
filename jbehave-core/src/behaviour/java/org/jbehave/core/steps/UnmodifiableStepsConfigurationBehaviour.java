package org.jbehave.core.steps;

import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.StepPatternParser;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnmodifiableStepsConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        StepsConfiguration delegate = new MostUsefulStepsConfiguration();
        StepsConfiguration unmodifiable = new UnmodifiableStepsConfiguration(delegate);
        assertThat(unmodifiable.keywords(), is(delegate.keywords()));
        assertThat(unmodifiable.monitor(), is(delegate.monitor()));
        assertThat(unmodifiable.paranamer(), is(delegate.paranamer()));
        assertThat(unmodifiable.patternParser(), is(delegate.patternParser()));
        assertThat(unmodifiable.parameterConverters(), is(delegate.parameterConverters()));
    }


    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException, IllegalAccessException {
        StepsConfiguration delegate = new MostUsefulStepsConfiguration();
        StepsConfiguration unmodifiable = new UnmodifiableStepsConfiguration(delegate);
        assertThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        assertThatNotAllowed(unmodifiable, "useMonitor", StepMonitor.class);
        assertThatNotAllowed(unmodifiable, "useParanamer", Paranamer.class);
        assertThatNotAllowed(unmodifiable, "usePatternParser", StepPatternParser.class);
        assertThatNotAllowed(unmodifiable, "useParameterConverters", ParameterConverters.class);
    }

    private void assertThatNotAllowed(StepsConfiguration unmodifiable, String methodName, Class<?> type) throws NoSuchMethodException, IllegalAccessException {
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