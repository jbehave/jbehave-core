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

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;

/**
 * Extends {@link AnnotationBuilder} to provide Guice-based 
 * dependency injection if {@link UsingGuice} annotation is present.
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

	@SuppressWarnings("unchecked")
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
				injector = rootInjector().createChildInjector(Modules.combine(modules));
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
			return new ParameterConverters().addConverters(findConverters(injector));
		}
		return super.parameterConverters(annotationFinder);
	}

	/**
	 * Finds any {@link ParameterConverter} defined in the given injector and, if none found,
	 * recurses to its parent.
	 * 
	 * @param injector
	 * @return A List of ParameterConverter instances
	 */
	private List<ParameterConverter> findConverters(Injector injector) {
		List<Binding<ParameterConverter>> bindingsByType = injector.findBindingsByType(new TypeLiteral<ParameterConverter>() {
		});
		if ((bindingsByType == null || bindingsByType.isEmpty() && injector.getParent() != null)) {
			return findConverters(injector.getParent());
		}
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (Binding<ParameterConverter> binding : bindingsByType) {
            if (binding != null) {
                converters.add(binding.getProvider().get());
            }
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
						// when binding not found, need to explicitly bind type + ofClass
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
	 * Finds binding for class in given injector and, if not found,
	 * recurses to its parent.
	 * 
	 * @param injector the current Inject
	 * @param type the Class
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

	private Injector rootInjector() {
		if (injector == null) {
			injector = Guice.createInjector(new AbstractModule() {

				@Override
				protected void configure() {

				}
			});
		}
		return injector;
	}
}
