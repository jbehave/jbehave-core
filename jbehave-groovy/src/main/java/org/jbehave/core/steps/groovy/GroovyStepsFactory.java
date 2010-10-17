package org.jbehave.core.steps.groovy;

import static java.lang.String.format;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;

public class GroovyStepsFactory extends AbstractStepsFactory {

    private final GroovyClassLoader classLoader = new GroovyClassLoader();
    private final List<String> groovyResources;

    public GroovyStepsFactory(Configuration configuration, List<String> groovyResources) {
        super(configuration);
        this.groovyResources = groovyResources;
    }

    @Override
    protected List<Object> stepsInstances() {
        List<Object> instances = new ArrayList<Object>();
        for (String resource : groovyResources) {
            instances.add(newInstanceFromScript(resource));                
        }
        return instances;
    }
    
    private Object newInstanceFromScript(String resource) {
        try {
            File file = new File(this.getClass().getResource(resource).toURI());
            return classLoader.parseClass(file).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(format("Could not create new instance from Groovy script '{0}'", resource), e);
        }
    }
}