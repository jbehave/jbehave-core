package org.jbehave.core.configuration.cdi;

import java.util.List;

import org.jbehave.core.annotations.cdi.UsingCDI;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDIAnnotationBuilder extends AnnotationBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(CDIAnnotationBuilder.class);
    
    private Configuration configuration;
    private InjectableStepsFactory stepsFactory;
    
    public CDIAnnotationBuilder(Class<?> annotatedClass) {
        this(annotatedClass, new PrintStreamAnnotationMonitor());
        
    }

    public CDIAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }
    
    public CDIAnnotationBuilder(Class<?> annotatedClass, Configuration configuration, InjectableStepsFactory stepsFactory)
    {
        this(annotatedClass);
        this.configuration = configuration;
        this.stepsFactory = stepsFactory;
    }

    @Override
    public Configuration buildConfiguration() throws AnnotationRequired {

        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingCDI.class)) {
            if(configuration == null) {
                LOG.debug("using default configuration");
                return super.buildConfiguration();
            }
            LOG.debug("Using injected config");
            return configuration;
        } else {
            annotationMonitor().annotationNotFound(UsingCDI.class, annotatedClass());
        }
        return super.buildConfiguration();
    }
    
    @Override
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        List<CandidateSteps> steps = super.buildCandidateSteps(configuration);
        if(stepsFactory != null) {
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
