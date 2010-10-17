package org.jbehave.core.steps.groovy;

import static java.lang.String.format;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;

public class GroovyStepsFactory extends AbstractStepsFactory {

    private final GroovyClassLoader classLoader;
    private final List<String> resources;

    public GroovyStepsFactory(Configuration configuration, GroovyResourceFinder resourceFinder) {
        this(configuration, new GroovyClassLoader(), resourceFinder.findResources());
    }

    public GroovyStepsFactory(Configuration configuration, List<String> resources) {
        this(configuration, new GroovyClassLoader(), resources);
    }

    public GroovyStepsFactory(Configuration configuration, GroovyClassLoader classLoader, List<String> resources) {
        super(configuration);
        this.classLoader = classLoader;
        this.resources = resources;
    }

    @Override
    protected List<Object> stepsInstances() {
        List<Object> instances = new ArrayList<Object>();
        for (String resource : resources) {
            Object object = newInstance(resource);
            if (hasAnnotatedMethods(object.getClass())) {
                instances.add(object);
            }
        }
        return instances;
    }

    private Object newInstance(String resource) {
        try {
            String name = resource.startsWith("/") ? resource : "/" + resource;
            File file = new File(this.getClass().getResource(name).toURI());
            return classLoader.parseClass(file).newInstance();
        } catch (Exception e) {
            throw new GroovyClassInstantiationFailed(classLoader, resource, e);
        }
    }

    @SuppressWarnings("serial")
    public static final class GroovyClassInstantiationFailed extends RuntimeException {

        public GroovyClassInstantiationFailed(GroovyClassLoader classLoader, String resource, Exception cause) {
            super(format("Failed to create new instance of class from resource '{0}' using Groovy class loader '{1}'",
                    resource, classLoader), cause);
        }

    }
}