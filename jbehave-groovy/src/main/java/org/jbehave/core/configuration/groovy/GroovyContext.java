package org.jbehave.core.configuration.groovy;

import static java.text.MessageFormat.format;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroovyContext {

    private final GroovyClassLoader classLoader;
    private final List<String> resources;
    private List<Object> instances;

    public GroovyContext() {
        this(new GroovyResourceFinder());
    }

    public GroovyContext(GroovyResourceFinder resourceFinder) {
        this(resourceFinder.findResources());
    }

    public GroovyContext(List<String> resources) {
        this(new BytecodeGroovyClassLoader(), resources);
    }

    public GroovyContext(GroovyClassLoader classLoader, GroovyResourceFinder resourceFinder) {
        this(classLoader, resourceFinder.findResources());
    }

    public GroovyContext(GroovyClassLoader classLoader, List<String> resources) {
        this.classLoader = classLoader;
        this.resources = resources;
        this.instances = createGroovyInstances();
    }

    public List<Object> getInstances() {
        return instances;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstanceOfType(Class<T> type) {
        for (Object instance : instances) {
            if (type.isAssignableFrom(instance.getClass())) {
                return (T) instance;
            }
        }
        throw new GroovyInstanceNotFound(type);
    }

    /**
     * Creates an object instance from the Groovy resource
     * 
     * @param resource the Groovy resource to parse
     * @return An Object instance
     */
    public Object newInstance(String resource) {
        try {
            String name = resource.startsWith("/") ? resource : "/" + resource;
            File file = new File(this.getClass().getResource(name).toURI());
            return newInstance(classLoader.parseClass(new GroovyCodeSource(file), true));
        } catch (Exception e) {
            throw new GroovyClassInstantiationFailed(classLoader, resource, e);
        }
    }

    /**
     * Creates an instance from the parsed Groovy class. This method can be
     * overriden to do some dependency injection on Groovy classes.
     * 
     * @param parsedClass the parsed Class to instantiate
     * @return An Object instance of the parsed Class
     * @throws Exception if instantiation or injection fails
     */
    public Object newInstance(Class<?> parsedClass) throws Exception {
        return parsedClass.newInstance();
    }

    private List<Object> createGroovyInstances() {
        List<Object> instances = new ArrayList<>();
        for (String resource : resources) {
            instances.add(newInstance(resource));
        }
        return instances;
    }

    @SuppressWarnings("serial")
    public static final class GroovyClassInstantiationFailed extends RuntimeException {

        public GroovyClassInstantiationFailed(GroovyClassLoader classLoader, String resource, Exception cause) {
            super(format("Failed to create new instance of class from resource {0} using Groovy class loader {1}",
                    resource, classLoader), cause);
        }

    }

    @SuppressWarnings("serial")
    public static final class GroovyInstanceNotFound extends RuntimeException {

        public GroovyInstanceNotFound(Class<?> type) {
            super(type.toString());
        }

    }

}
