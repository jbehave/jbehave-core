package org.jbehave.core.configuration.pico;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.pico.UsingPico;
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
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AbstractInjector.AmbiguousComponentResolutionException;

/**
 * Extends {@link AnnotationBuilder} to provide PicoContainer-based 
 * dependency injection if {@link UsingPico} annotation is present.
 * 
 * @author Cristiano Gavião
 * @author Mauro Talevi
 */
public class PicoAnnotationBuilder extends AnnotationBuilder {

    private PicoContainer container;

    public PicoAnnotationBuilder(Class<?> annotatedClass) {
        super(annotatedClass);
    }

    public PicoAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    public Configuration buildConfiguration() throws AnnotationRequired {
        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingPico.class)) {
            @SuppressWarnings("rawtypes")
            List<Class> moduleClasses = finder.getAnnotatedValues(UsingPico.class, Class.class, "modules");
            List<PicoModule> modules = new ArrayList<PicoModule>();
            for (Class<PicoModule> moduleClass : moduleClasses) {
                try {
                    modules.add(moduleClass.newInstance());
                } catch (Exception e) {
                    annotationMonitor().elementCreationFailed(moduleClass, e);
                }
            }
            if ( modules.size() > 0 ){
                container = createPicoContainer(modules);                
            }
        } else {
            annotationMonitor().annotationNotFound(UsingPico.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
        InjectableStepsFactory factoryUsingSteps = super.buildStepsFactory(configuration);
        if (container != null) {
            return new CompositeStepsFactory(new PicoStepsFactory(configuration, container), factoryUsingSteps);
        }
        return factoryUsingSteps;
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder, ResourceLoader resourceLoader,
            TableTransformers tableTransformers) {
        ParameterConverters converters = super.parameterConverters(annotationFinder, resourceLoader, tableTransformers);
        if (container != null) {
            return converters.addConverters(container.getComponents(ParameterConverter.class));
        }
        return converters;
    }

    @Override
    protected <T, V extends T> T instanceOf(final Class<T> type, final Class<V> ofClass) {
        if (container != null) {
            T instance = null;            
            try {
                instance = container.getComponent(type);
            } catch (AmbiguousComponentResolutionException e) {
                instance = container.getComponent(ofClass);
            }
            if ( instance != null ){
                return instance;
            }
        }
        return super.instanceOf(type, ofClass);
    }

    @SuppressWarnings("unchecked")
    protected PicoContainer createPicoContainer(List<PicoModule> modules) {
        if ( container != null ){
            return container;
        }
        @SuppressWarnings("rawtypes")
        Class containerClass = annotationFinder().getAnnotatedValue(UsingPico.class, Class.class, "container");
        MutablePicoContainer container = instanceOf(MutablePicoContainer.class, containerClass);
        for (PicoModule module : modules) {
            module.configure(container);
        }
        return container;
    }

    protected PicoContainer picoContainer() {
        return container;
    }

}
