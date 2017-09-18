package org.jbehave.core.configuration.guice;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.CompositeStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.guice.GuiceStepsFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;

/**
 * Extends {@link AnnotationBuilder} to provide Guice-based dependency injection
 * if {@link UsingGuice} annotation is present.
 * 
 * @author Cristiano Gavião
 * @author Mauro Talevi
 */
public class GuiceAnnotationBuilder extends AnnotationBuilder {

    public Injector injector;

    public GuiceAnnotationBuilder(Class<?> annotatedClass) {
        this(annotatedClass, new PrintStreamAnnotationMonitor());
    }

    public GuiceAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    public Configuration buildConfiguration() throws AnnotationRequired {

        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingGuice.class)) {
            @SuppressWarnings("rawtypes")
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
                injector = createInjector(modules);
            }
        } else {
            annotationMonitor().annotationNotFound(UsingGuice.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
        InjectableStepsFactory factoryUsingSteps = super.buildStepsFactory(configuration);
        if (injector != null) {
            return new CompositeStepsFactory(new GuiceStepsFactory(configuration, injector), factoryUsingSteps);
        }
        return factoryUsingSteps;
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder, ResourceLoader resourceLoader,
            TableTransformers tableTransformers) {
        ParameterConverters converters = super.parameterConverters(annotationFinder, resourceLoader, tableTransformers);
        if (injector != null) {
            return converters.addConverters(findConverters(injector));
        }
        return converters;
    }

    /**
     * Finds any {@link ParameterConverter} defined in the given injector and,
     * if none found, recurses to its parent.
     * 
     * @param injector
     *            the Injector
     * @return A List of ParameterConverter instances
     */
    private List<ParameterConverter> findConverters(Injector injector) {
        List<Binding<ParameterConverter>> bindingsByType = injector
                .findBindingsByType(new TypeLiteral<ParameterConverter>() {
                });
        if (bindingsByType.isEmpty() && injector.getParent() != null) {
            return findConverters(injector.getParent());
        }
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (Binding<ParameterConverter> binding : bindingsByType) {
            converters.add(binding.getProvider().get());
        }
        return converters;
    }

    @Override
    protected <T, V extends T> T instanceOf(final Class<T> type, final Class<V> ofClass) {
        if (injector != null) {
            if (!type.equals(Object.class)) {
                try {
                    boolean bindingFound = findBinding(injector, type);
                    if (bindingFound) {
                        // when binding found, just get the instance associated
                        return injector.getInstance(type);
                    } else {
                        // when binding not found, need to explicitly bind type
                        // + ofClass
                        Module module = new AbstractModule() {

                            @Override
                            protected void configure() {
                                if (!type.equals(ofClass)) {
                                    bind(type).to(ofClass);
                                } else {
                                    // when type and oFClass are
                                    // binding the ofClass
                                    bind(ofClass);
                                }
                            }
                        };

                        injector = injector.createChildInjector(module);
                        return injector.getInstance(type);
                    }
                } catch (Exception e) {
                    // fall back on getting instance ofClass
                    return injector.getInstance(ofClass);
                }
            } else {
                return injector.getBinding(ofClass).getProvider().get();
            }
        }
        return super.instanceOf(type, ofClass);
    }

    /**
     * Finds binding for a type in the given injector and, if not found,
     * recurses to its parent
     * 
     * @param injector
     *            the current Injector
     * @param type
     *            the Class representing the type
     * @return A boolean flag, <code>true</code> if binding found
     */
    private boolean findBinding(Injector injector, Class<?> type) {
        boolean found = false;
        for (Key<?> key : injector.getBindings().keySet()) {
            if (key.getTypeLiteral().getRawType().equals(type)) {
                found = true;
                break;
            }
        }
        if (!found && injector.getParent() != null) {
            return findBinding(injector.getParent(), type);
        }

        return found;
    }

    protected Injector createInjector(List<Module> modules) {
        if ( injector != null ){
            return injector;
        }
        Injector root = Guice.createInjector(new AbstractModule() {        
            @Override
            protected void configure() {
        
            }
        });
        return root.createChildInjector(Modules.combine(modules));
    }

    protected Injector injector() {
        return injector;
    }
}
