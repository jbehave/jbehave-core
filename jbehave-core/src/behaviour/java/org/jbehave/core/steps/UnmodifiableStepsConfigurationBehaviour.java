package org.jbehave.core.steps;

import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parser.StepPatternBuilder;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.jbehave.Ensure.ensureThat;

public class UnmodifiableStepsConfigurationBehaviour {

    @Test
    public void shouldProvideDelegateConfigurationElements() {
        StepsConfiguration delegate = new MostUsefulStepsConfiguration();
        StepsConfiguration unmodifiable = new UnmodifiableStepsConfiguration(delegate);
        ensureThat(unmodifiable.keywords(), is(delegate.keywords()));
        ensureThat(unmodifiable.monitor(), is(delegate.monitor()));
        ensureThat(unmodifiable.paranamer(), is(delegate.paranamer()));
        ensureThat(unmodifiable.patternBuilder(), is(delegate.patternBuilder()));
        ensureThat(unmodifiable.parameterConverters(), is(delegate.parameterConverters()));
    }


    @Test
    public void shouldNotAllowModificationOfConfigurationElements() throws NoSuchMethodException, IllegalAccessException {
        StepsConfiguration delegate = new MostUsefulStepsConfiguration();
        StepsConfiguration unmodifiable = new UnmodifiableStepsConfiguration(delegate);
        ensureThatNotAllowed(unmodifiable, "useKeywords", Keywords.class);
        ensureThatNotAllowed(unmodifiable, "useMonitor", StepMonitor.class);
        ensureThatNotAllowed(unmodifiable, "useParanamer", Paranamer.class);
        ensureThatNotAllowed(unmodifiable, "usePatternBuilder", StepPatternBuilder.class);
        ensureThatNotAllowed(unmodifiable, "useParameterConverters", ParameterConverters.class);
    }

    private void ensureThatNotAllowed(StepsConfiguration unmodifiable, String methodName, Class<?> type) throws NoSuchMethodException, IllegalAccessException {
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