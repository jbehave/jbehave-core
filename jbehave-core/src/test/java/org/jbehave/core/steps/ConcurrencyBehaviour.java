package org.jbehave.core.steps;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ConcurrencyBehaviour {

    @Test
    public void shouldAllowConcurrentAdditionOfParameterConvertersInThreadSafeMode() {

        final ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath(),
                new TableTransformers(), true);

        final boolean[] active = new boolean[] { true };
        final ParameterConverter[] toAdd = new ParameterConverter[] { mock(ParameterConverter.class, "one"),
                mock(ParameterConverter.class, "two"), mock(ParameterConverter.class, "three") };
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active[0]) {
                    parameterConverters.addConverters(toAdd);
                }
            }
        });
        t.start();
        parameterConverters.convert("test", String.class);
        active[0] = false;
    }
}