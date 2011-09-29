package org.jbehave.core.configuration.scala;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScalaContext {

    private final ClassLoader classLoader;
    private final List<String> classNames;
    private List<Object> instances;

    public ScalaContext(String... classNames) {
        this(ScalaContext.class.getClassLoader(), classNames);
    }
    
    public ScalaContext(ClassLoader classLoader, String... classNames) {
        this.classLoader = classLoader;
        this.classNames = Arrays.asList(classNames);
        this.instances = createInstances();
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
        throw new ScalaInstanceNotFound(type);
    }

    /**
     * Creates an object instance from the Scala class name
     * 
     * @param className the Scala class name
     * @return An Object instance
     */
    public Object newInstance(String className) {
        try {
            return classLoader.loadClass(className).newInstance();
        } catch (Exception e) {
            throw new ScalaInstanceNotFound(className);
        }
    }

    private List<Object> createInstances() {
        List<Object> instances = new ArrayList<Object>();
        for (String className : classNames) {
            instances.add(newInstance(className));
        }
        return instances;
    }

    @SuppressWarnings("serial")
    public static final class ScalaInstanceNotFound extends RuntimeException {

        public ScalaInstanceNotFound(Class<?> type) {
            super(type.toString());
        }

        public ScalaInstanceNotFound(String className) {
            super(className);
        }

    }

}