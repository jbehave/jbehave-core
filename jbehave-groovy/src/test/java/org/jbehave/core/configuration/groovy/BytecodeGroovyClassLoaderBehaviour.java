package org.jbehave.core.configuration.groovy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class BytecodeGroovyClassLoaderBehaviour {

    @Test
    public void shouldCacheBytes() throws IOException {
        GroovyClassLoader classLoader = new BytecodeGroovyClassLoader();
        assertThat((Class<?>) classLoader.parseClass("class Hello { }"), is(notNullValue()));
        InputStream bytecode = classLoader.getResourceAsStream("Hello.class");
        assertThat(bytecode, is(notNullValue()));
        bytecode.close();
    }

}
