package org.jbehave.core.configuration.weld;

import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.steps.CompositeStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters;

/**
 * Extends {@link AnnotationBuilder} to provide Weld-based dependency injection
 * if {@link UsingWeld} annotation is present.
 * 
 * @author Aaron Walker
 */
public class WeldAnnotationBuilder extends AnnotationBuilder {

    private Configuration configuration;
    private InjectableStepsFactory stepsFactory;

    public WeldAnnotationBuilder(Class<?> annotatedClass) {
        this(annotatedClass, new PrintStreamAnnotationMonitor());
    }

    public WeldAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    public WeldAnnotationBuilder(Class<?> annotatedClass, Configuration configuration,
            InjectableStepsFactory stepsFactory) {
        this(annotatedClass);
        this.configuration = configuration;
        this.stepsFactory = stepsFactory;
    }

    @Override
    public Configuration buildConfiguration() throws AnnotationRequired {
        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingWeld.class)) {
            if (configuration == null) {
                return super.buildConfiguration();
            }
            return configuration;
        } else {
            annotationMonitor().annotationNotFound(UsingWeld.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
        InjectableStepsFactory factoryUsingSteps = super.buildStepsFactory(configuration);
        if (stepsFactory != null) {
            return new CompositeStepsFactory(stepsFactory, factoryUsingSteps);
        }
        return factoryUsingSteps;
    }
}
