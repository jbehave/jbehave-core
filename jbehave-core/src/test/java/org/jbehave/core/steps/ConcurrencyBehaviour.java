package org.jbehave.core.steps;

import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConcurrencyBehaviour {

    @Test
    public void concurrentModificationToParameterConvertersThrowsException() {

        final ParameterConverters parameterConverters = new ParameterConverters();

        final boolean[] active = new boolean[]{true};
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (active[0]) {
                    parameterConverters.addConverters(new ParameterConverters.ParameterConverter() {
                        public boolean accept(Type type) {
                            return false;
                        }

                        public Object convertValue(String value, Type type) {
                            return null;
                        }
                    });
                }
            }
        });
        t.start();
        parameterConverters.convert("test", String.class);
        active[0] = false;
    }
}