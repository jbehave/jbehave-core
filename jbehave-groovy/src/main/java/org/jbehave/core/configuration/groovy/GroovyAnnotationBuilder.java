package org.jbehave.core.configuration.groovy;

import org.jbehave.core.annotations.groovy.UsingGroovy;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CompositeStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;

import groovy.lang.GroovyClassLoader;

/**
 * Extends {@link AnnotationBuilder} using Groovy-based resources if
 * {@link UsingGroovy} annotation is present.
 * 
 * @author Mauro Talevi
 */
public class GroovyAnnotationBuilder extends AnnotationBuilder {

    private GroovyContext context;

    public GroovyAnnotationBuilder(Class<?> annotatedClass) {
        super(annotatedClass);
    }

    public GroovyAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Configuration buildConfiguration() throws AnnotationRequired {
        if (annotationFinder().isAnnotationPresent(UsingGroovy.class)) {
            Class<GroovyClassLoader> classLoaderClass = annotationFinder().getAnnotatedValue(UsingGroovy.class,
                    Class.class, "classLoader");
            Class<GroovyResourceFinder> resourceFinderClass = annotationFinder().getAnnotatedValue(UsingGroovy.class,
                    Class.class, "resourceFinder");
            try {
                GroovyClassLoader classLoader = super.instanceOf(classLoaderClass, classLoaderClass);
                GroovyResourceFinder resourceFinder = super.instanceOf(resourceFinderClass, resourceFinderClass);
                context = createGroovyContext(classLoader, resourceFinder);
            } catch (Exception e) {
                annotationMonitor().elementCreationFailed(GroovyContext.class, e);
            }
        } else {
            annotationMonitor().annotationNotFound(UsingGroovy.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
        InjectableStepsFactory factoryUsingSteps = super.buildStepsFactory(configuration);
        if (context != null) {
            return new CompositeStepsFactory(new GroovyStepsFactory(configuration, context), factoryUsingSteps);
        }
        return factoryUsingSteps;
    }
    
    @Override
    protected <T, V extends T> T instanceOf(Class<T> type, Class<V> ofClass) {
        if (context != null) {
            try {
                return context.getInstanceOfType(type);
            } catch (Exception e) {
                // default to super class
            }
        }
        return super.instanceOf(type, ofClass);
    }

    protected GroovyContext createGroovyContext(GroovyClassLoader classLoader, GroovyResourceFinder resourceFinder) {
        if (context != null) {
            return context;
        }
        return new GroovyContext(classLoader, resourceFinder);
    }

}
