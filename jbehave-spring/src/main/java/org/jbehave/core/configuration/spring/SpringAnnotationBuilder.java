package org.jbehave.core.configuration.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.springframework.context.ApplicationContext;

/**
 * Extends {@link AnnotationBuilder} to provide Spring-based dependency
 * injection if {@link UsingSpring} annotation is present.
 * 
 * @author Cristiano Gavião
 * @author Mauro Talevi
 */
public class SpringAnnotationBuilder extends AnnotationBuilder {

    private ApplicationContext context;

    public SpringAnnotationBuilder(Class<?> annotatedClass) {
        super(annotatedClass);
    }

    public SpringAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    @Override
    public Configuration buildConfiguration() throws AnnotationRequired {
        if (annotationFinder().isAnnotationPresent(UsingSpring.class)) {
            if (annotationFinder().isAnnotationValuePresent(UsingSpring.class, "resources")) {
                List<String> resources = annotationFinder().getAnnotatedValues(UsingSpring.class, String.class,
                        "resources");
                context = applicationContextFor(annotatedClass().getClassLoader(), resources);
            }
        } else {
            annotationMonitor().annotationNotFound(UsingSpring.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        List<CandidateSteps> steps = super.buildCandidateSteps(configuration);
        if (context != null) {
            InjectableStepsFactory factory = new SpringStepsFactory(configuration, context);
            steps.addAll(0, factory.createCandidateSteps());
        }
        return steps;
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        ParameterConverters converters = super.parameterConverters(annotationFinder);
        if (context != null) {
            return converters.addConverters(getBeansOfType(context, ParameterConverter.class));
        }
        return converters;
    }

    @SuppressWarnings("unchecked")
    private List<ParameterConverter> getBeansOfType(ApplicationContext context, Class<ParameterConverter> type) {
        Map<String, ParameterConverter> beansOfType = context.getBeansOfType(type);
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (ParameterConverter converter : beansOfType.values()) {
            converters.add(converter);
        }
        return converters;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T, V extends T> T instanceOf(final Class<T> type, final Class<V> ofClass) {
        if (context != null) {
            if ( !type.equals(Object.class) ){
                Map<String, Object> beansOfType = context.getBeansOfType(type);
                if (beansOfType.size() > 0) {
                    return (T) beansOfType.values().iterator().next();
                }                
            } else {
                Map<String, Object> beansOfType = context.getBeansOfType(ofClass);
                if (beansOfType.size() > 0) {
                    return (T) beansOfType.values().iterator().next();
                }                                
            }
        }
        return super.instanceOf(type, ofClass);
    }

    private ApplicationContext applicationContextFor(ClassLoader classLoader, List<String> resources) {
        return new SpringApplicationContextFactory(classLoader, resources.toArray(new String[resources.size()]))
                .createApplicationContext();
    }

}
