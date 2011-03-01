package org.jbehave.core.configuration.weld;

import java.util.List;

import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.steps.CandidateSteps;
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
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        List<CandidateSteps> steps = super.buildCandidateSteps(configuration);
        if (stepsFactory != null) {
            steps.addAll(0, stepsFactory.createCandidateSteps());
        }
        return steps;
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        ParameterConverters converters = super.parameterConverters(annotationFinder);
        return converters;
    }
}
