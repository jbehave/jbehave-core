package org.jbehave.core.configuration.groovy;

import static junit.framework.Assert.assertNotNull;
import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class BytecodeGroovyClassLoaderBehaviour {

    @Test
    public void shouldCacheBytes() throws IOException {
        GroovyClassLoader classLoader = new BytecodeGroovyClassLoader();
        assertNotNull((Class<?>) classLoader.parseClass("class Hello { }"));
        InputStream bytecode = classLoader.getResourceAsStream("Hello.class");
        assertNotNull(bytecode);
        bytecode.close();
    }

}
