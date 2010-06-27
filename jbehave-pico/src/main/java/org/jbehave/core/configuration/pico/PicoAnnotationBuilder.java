package org.jbehave.core.configuration.pico;

import java.util.List;

import org.jbehave.core.annotations.pico.UsingPico;
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
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.picocontainer.PicoContainer;

public class PicoAnnotationBuilder extends AnnotationBuilder {

    private PicoContainer container;

    public PicoAnnotationBuilder(Class<?> annotatedClass) {
        super(annotatedClass);
    }

    public PicoAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Configuration buildConfiguration() throws MissingAnnotationException {

        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingPico.class)) {
            List<Class> containerClasses = finder.getAnnotatedValues(UsingPico.class, Class.class, "containers");
            if ( containerClasses.size() > 0 ){
                container = instanceOf(PicoContainer.class, containerClasses.iterator().next());                
            }
        } else {
            annotationMonitor().annotationNotFound(UsingPico.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps() {
        Configuration configuration = buildConfiguration();
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration);
        if (container != null) {
            factory = new PicoStepsFactory(configuration, container);
        }
        return factory.createCandidateSteps();
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        if (container != null) {
            List<ParameterConverter> converters = container.getComponents(ParameterConverter.class);
            return new ParameterConverters().addConverters(converters);
        }
        return super.parameterConverters(annotationFinder);
    }

    @Override
    protected <T> T instanceOf(Class<T> type, Class<T> ofClass) {
        if (container != null) {
            T instance = container.getComponent(type);
            if ( instance != null ){
                return instance;
            } else {
                // fall back on default
                // getAnnotationMonitor().elementCreationFailed(type, e);
            }
        }
        return super.instanceOf(type, ofClass);
    }

}
