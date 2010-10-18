package org.jbehave.core.configuration.groovy;

import org.jbehave.core.annotations.groovy.UsingGroovy;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;

import java.util.List;

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
            Class<JBehaveGroovyClassLoader> classLoaderClass = annotationFinder().getAnnotatedValue(UsingGroovy.class,
                    Class.class, "classLoader");
            Class<GroovyResourceFinder> resourceFinderClass = annotationFinder().getAnnotatedValue(UsingGroovy.class,
                    Class.class, "resourceFinder");
            try {
                JBehaveGroovyClassLoader classLoader = super.instanceOf(classLoaderClass, classLoaderClass);
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
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        List<CandidateSteps> steps = super.buildCandidateSteps(configuration);
        if (context != null) {
            InjectableStepsFactory factory = new GroovyStepsFactory(configuration, context);
            steps.addAll(0, factory.createCandidateSteps());
        }
        return steps;
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

    protected GroovyContext createGroovyContext(JBehaveGroovyClassLoader classLoader, GroovyResourceFinder resourceFinder) {
        if (context != null) {
            return context;
        }
        return new GroovyContext(classLoader, resourceFinder);
    }

}
