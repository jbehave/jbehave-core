package org.jbehave.core.configuration.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.CompositeStepsFactory;
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
            List<String> resources = annotationFinder()
                    .getAnnotatedValues(UsingSpring.class, String.class, "resources");
            if (resources.size() > 0) {
                try {
                    context = createApplicationContext(annotatedClass().getClassLoader(), resources);
                } catch (Exception e) {
                    annotationMonitor().elementCreationFailed(ApplicationContext.class, e);
                    boolean ignoreContextFailure = annotationFinder().getAnnotatedValue(UsingSpring.class, boolean.class, "ignoreContextFailure");
					if ( !ignoreContextFailure ){
                    	throw new InstantiationFailed(annotatedClass(), e);
                    }
                }
            }
        } else {
            annotationMonitor().annotationNotFound(UsingSpring.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
        InjectableStepsFactory factoryUsingSteps = super.buildStepsFactory(configuration);
        if (context != null) {
            return new CompositeStepsFactory(new SpringStepsFactory(configuration, context), factoryUsingSteps);
        }
        return factoryUsingSteps;
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder, ResourceLoader resourceLoader,
            TableTransformers tableTransformers) {
        ParameterConverters converters = super.parameterConverters(annotationFinder, resourceLoader, tableTransformers);
        if (context != null) {
            return converters.addConverters(getBeansOfType(context, ParameterConverter.class));
        }
        return converters;
    }

    private List<ParameterConverter> getBeansOfType(ApplicationContext context, Class<ParameterConverter> type) {
        Map<String, ParameterConverter> beansOfType = context.getBeansOfType(type);
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (ParameterConverter converter : beansOfType.values()) {
            converters.add(converter);
        }
        return converters;
    }

    @Override
    protected <T, V extends T> T instanceOf(Class<T> type, Class<V> ofClass) {
        if (context != null) {
            if (!type.equals(Object.class)) {
                if (context.getBeansOfType(type).size() > 0) {
                    return context.getBeansOfType(type).values().iterator().next();
                }
            } else {
                if (context.getBeansOfType(ofClass).size() > 0) {
                    return context.getBeansOfType(ofClass).values().iterator().next();
                }
            }
        }
        return super.instanceOf(type, ofClass);
    }

    protected ApplicationContext createApplicationContext(ClassLoader classLoader, List<String> resources) {
        if (context != null) {
            return context;
        }
        return new SpringApplicationContextFactory(classLoader, resources.toArray(new String[resources.size()]))
                .createApplicationContext();
    }

    protected ApplicationContext applicationContext() {
        return context;
    }

}
