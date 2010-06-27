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
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.springframework.context.ApplicationContext;

public class SpringAnnotationBuilder extends AnnotationBuilder {

    private ApplicationContext context;

    public SpringAnnotationBuilder(Class<?> annotatedClass) {
    	super(annotatedClass);
    }

    public SpringAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }
    
    @Override
    public Configuration buildConfiguration() throws MissingAnnotationException {
        if (annotationFinder().isAnnotationPresent(UsingSpring.class)) {
            if (annotationFinder().isAnnotationValuePresent(UsingSpring.class, "locations")) {
                List<String> locations = annotationFinder().getAnnotatedValues(UsingSpring.class, String.class, "locations");
                context = applicationContextFor(locations);
            }
        } else {
        	annotationMonitor().annotationNotFound(UsingSpring.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps() {
        Configuration configuration = buildConfiguration();
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
