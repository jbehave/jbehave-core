package org.jbehave.core.configuration.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MissingAnnotationException;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.springframework.context.ApplicationContext;

public class SpringAnnotationBuilder extends AnnotationBuilder {

    private final AnnotationMonitor annotationMonitor;
    private ApplicationContext context;

    public SpringAnnotationBuilder() {
        this(new PrintStreamAnnotationMonitor());
    }

    public SpringAnnotationBuilder(AnnotationMonitor annotationMonitor) {
        this.annotationMonitor = annotationMonitor;
    }
    
    @Override
    public Configuration buildConfiguration(Object annotatedInstance) throws MissingAnnotationException {
        AnnotationFinder finder = new AnnotationFinder(annotatedInstance.getClass());
        if (finder.isAnnotationPresent(UsingSpring.class)) {
            if (finder.isAnnotationValuePresent(UsingSpring.class, "locations")) {
                List<String> locations = finder.getAnnotatedValues(UsingSpring.class, String.class, "locations");
                context = applicationContextFor(locations);
            }
        } else {
            annotationMonitor.annotationNotFound(UsingSpring.class, annotatedInstance);
        }
        return super.buildConfiguration(annotatedInstance);
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps(Object annotatedInstance) {
        Configuration configuration = buildConfiguration(annotatedInstance);
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration);
        if ( context != null ){
            factory = new SpringStepsFactory(configuration, context);            
        }
        return factory.createCandidateSteps();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        if ( context != null ){
            Map<String,ParameterConverter> beansOfType = context.getBeansOfType(ParameterConverter.class);
            List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
            for ( ParameterConverter converter : beansOfType.values() ) {
                converters.add(converter);
            }
            return new ParameterConverters().addConverters(converters);
        }
        return super.parameterConverters(annotationFinder);
    }


    @SuppressWarnings("unchecked")
    @Override
    protected <T> T instanceOf(Class<T> type, Class<T> ofClass) {
        if ( context != null ){
            Map<String,Object> beansOfType = context.getBeansOfType(type);
            if ( beansOfType.size() > 0 ){
                return (T) beansOfType.values().iterator().next();
            } 
        }
        return super.instanceOf(type, ofClass);
    }
    
    private ApplicationContext applicationContextFor(List<String> locations) {
        return new SpringApplicationContextFactory(locations.toArray(new String[locations
                .size()])).createApplicationContext();
    }
    

}
