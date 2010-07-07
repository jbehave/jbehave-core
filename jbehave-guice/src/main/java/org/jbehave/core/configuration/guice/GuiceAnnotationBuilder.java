package org.jbehave.core.configuration.guice;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.guice.UsingGuice;
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
import org.jbehave.core.steps.guice.GuiceStepsFactory;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;

public class GuiceAnnotationBuilder extends AnnotationBuilder {

    public Injector injector;

    public GuiceAnnotationBuilder(Class<?> annotatedClass) {
        this(annotatedClass, new PrintStreamAnnotationMonitor());
    }

    public GuiceAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    @SuppressWarnings("rawtypes")
    public Configuration buildConfiguration() throws MissingAnnotationException {

        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingGuice.class)) {
            List<Class> moduleClasses = finder.getAnnotatedValues(UsingGuice.class, Class.class, "modules");
            List<Module> modules = new ArrayList<Module>();
            for (Class<Module> moduleClass : moduleClasses) {
                try {
                    modules.add(moduleClass.newInstance());
                } catch (Exception e) {
                    annotationMonitor().elementCreationFailed(moduleClass, e);
                }
            }
            // creating injector with any modules found
            if (modules.size() > 0) {
                injector = Guice.createInjector(Modules.combine(modules));
            }
        } else {
            annotationMonitor().annotationNotFound(UsingGuice.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        InjectableStepsFactory factory;
        if ( injector != null ){
            factory = new GuiceStepsFactory(configuration, injector);            
        } else {
            factory = new InstanceStepsFactory(configuration);
        }
        return factory.createCandidateSteps();
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        if (injector != null) {
            List<Binding<ParameterConverter>> bindingsByType = injector
                    .findBindingsByType(new TypeLiteral<ParameterConverter>() {});
            List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
            for (Binding<ParameterConverter> binding : bindingsByType) {
                converters.add(binding.getProvider().get());
            }
            return new ParameterConverters().addConverters(converters);
        }
        return super.parameterConverters(annotationFinder);
    }

    @Override
    protected <T> T instanceOf(Class<T> type, Class<T> ofClass) {
        if (injector != null) {
            try {
                if (!type.equals(Object.class) && injector.getBinding(type) != null) {
                    return injector.getInstance(type);
                }
            } catch (Exception e) {
                // fall back on getting instance by class
            }
            return injector.getInstance(ofClass);
        }
        return super.instanceOf(type, ofClass);
    }

}
