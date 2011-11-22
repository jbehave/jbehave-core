package org.jbehave.core.embedder;

import groovy.lang.GroovyClassLoader;
import org.jbehave.core.model.Meta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GroovyMetaMatcher implements MetaFilter.MetaMatcher {

    private Class groovyClass;
    private Field metaField;
    private Method match;

    public void parse(String filterAsString) {
        String groovyString = "public class GroovyMatcher {" +
                "public org.jbehave.core.model.Meta meta\n" +
                "public boolean match() {\n" +
                "  return (" + filterAsString.substring("groovy: ".length()) + ")\n" +
                "}\n" +
                "def propertyMissing(String name) {\n" +
                "  return meta.getProperty(name)\n" +
                "}\n" +
                "}";

        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        groovyClass = loader.parseClass(groovyString);
        try {
            match = groovyClass.getDeclaredMethod("match");
            metaField = groovyClass.getField("meta");
        } catch (NoSuchFieldException e) {
        } catch (NoSuchMethodException e) {
        }
        System.out.println();
    }

    public boolean match(Meta meta) {
        try {
            Object matcher = groovyClass.newInstance();
            metaField.set(matcher, meta);
            return (Boolean) match.invoke(matcher);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
