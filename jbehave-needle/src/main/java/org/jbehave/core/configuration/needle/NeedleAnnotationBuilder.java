package org.jbehave.core.configuration.needle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.needle.UsingNeedle;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.AnnotationRequired;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.needle.NeedleStepsFactory;
import org.jbehave.core.steps.needle.configuration.CreateInstanceByDefaultConstructor;
import org.needle4j.injection.InjectionProvider;
import org.needle4j.injection.InjectionProviderInstancesSupplier;

/**
 * Extends {@link AnnotationBuilder} to provide Needle-based dependency injection if {@link UsingNeedle} annotation is
 * present.
 * 
 * @author Simon Zambrovski (simon.zambrovski@holisticon.de)
 * @author Jan Galinski (jan.galinski@holisticon.de)
 * 
 */
public class NeedleAnnotationBuilder extends AnnotationBuilder {

	private final Set<InjectionProvider<?>> provider = new HashSet<>();
	private final Set<Class<?>> stepsClasses = new HashSet<>();
	private NeedleStepsFactory factory;

	public NeedleAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
		super(annotatedClass, annotationMonitor);
	}

	public NeedleAnnotationBuilder(Class<?> annotatedClass) {
		this(annotatedClass, new PrintStreamAnnotationMonitor());
	}

	@Override
	public Configuration buildConfiguration() throws AnnotationRequired {

		final AnnotationFinder finder = annotationFinder();

		if (finder.isAnnotationPresent(UsingSteps.class)) {
			stepsClasses.addAll(finder.getAnnotatedClasses(UsingSteps.class, Object.class, "instances"));
		}

		if (finder.isAnnotationPresent(UsingNeedle.class)) {

			@SuppressWarnings("rawtypes")
			final List<Class> supplierClasses = finder.getAnnotatedValues(UsingNeedle.class, Class.class, "supplier");
			for (Class<InjectionProviderInstancesSupplier> supplierClass : supplierClasses) {
				provider.addAll(CreateInstanceByDefaultConstructor.INSTANCE.apply(supplierClass).get());
			}

			@SuppressWarnings("rawtypes")
			final List<Class> providerClasses = finder.getAnnotatedValues(UsingNeedle.class, Class.class, "provider");
			for (Class<InjectionProvider<?>> providerClass : providerClasses) {
				provider.add(CreateInstanceByDefaultConstructor.INSTANCE.apply(providerClass));
			}

		} else {
			annotationMonitor().annotationNotFound(UsingNeedle.class, annotatedClass());
		}
		return super.buildConfiguration();
	}

	@Override
	public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
		this.factory = new NeedleStepsFactory(configuration, provider, stepsClasses.toArray(new Class<?>[stepsClasses
				.size()]));
		return factory;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T, V extends T> T instanceOf(Class<T> type, Class<V> ofClass) {
		/*
		 * This allow usage of the factory only after step factory is constructed. Current implementation only supports
		 * creation injection into steps. Further improvement will be to provide a needle factory capable of creating
		 * configuration parts.
		 */
		if (factory != null) {
			return (T) factory.createInstanceOfType(ofClass);
		}
		return super.instanceOf(type, ofClass);
	}

	/**
	 * Retrieves the set of injection providers.
	 * 
	 * @return set of providers.
	 */
	public Set<InjectionProvider<?>> getProvider() {
		return provider;
	}

	/**
	 * Retrieve step classes.
	 * 
	 * @return set of step classes.
	 */
	public Set<Class<?>> getStepsClasses() {
		return stepsClasses;
	}
}
