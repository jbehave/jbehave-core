package org.jbehave.core.configuration.groovy;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertNotNull;

public class JBehaveGroovyClassLoaderBehaviour {

    @Test
    public void shouldCacheBytes() throws IOException {

        JBehaveGroovyClassLoader cl = new JBehaveGroovyClassLoader();
        Class clazz = cl.parseClass("class Hello { }");
        assertNotNull(clazz);
        InputStream bytecode = cl.getResourceAsStream("Hello.class");
        assertNotNull(bytecode);
        bytecode.close();

    }

}
