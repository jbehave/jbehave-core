package org.jbehave.core.steps.needle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.needle.configuration.CollectInjectionProvidersFromStepsInstance;
import org.jbehave.core.steps.needle.configuration.CreateInstanceByDefaultConstructor;
import org.jbehave.core.steps.needle.configuration.JBehaveNeedleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

/**
 * An {@link InjectableStepsFactory} that uses a Needle {@link InjectionProvider} for the composition and instantiation
 * of all components that contain JBehave annotated methods.
 * 
 * @author Simon Zambrovski (simon.zambrovski@holisticon.de)
 * @author Jan Galinski (jan.galinski@holisticon.de)
 */
public class NeedleStepsFactory extends NeedleTestcase implements InjectableStepsFactory {

	private final Map<Class<?>, Object> cachedStepsInstances = new LinkedHashMap<Class<?>, Object>();

	private final Logger logger = LoggerFactory.getLogger(NeedleStepsFactory.class);
	private final Configuration configuration;
	private Class<?>[] steps;

	/**
	 * Creates factory with given configuration and step instances.
	 * 
	 * @param configuration
	 *            JBehave configuration
	 * @param steps
	 *            step classes
	 */
	public NeedleStepsFactory(final Configuration configuration, final Class<?>... steps) {
		this(configuration, null, steps);
	}

	/**
	 * Creates factory with given configuration, injection providers and step instances.
	 * 
	 * @param configuration
	 *            JBehave configuration
	 * @param steps
	 *            step classes
	 * @param providers
	 *            injection providers.
	 */
	public NeedleStepsFactory(final Configuration configuration, final Set<InjectionProvider<?>> injectionProviders,
			final Class<?>... steps) {
		super(setUpInjectionProviders(JBehaveNeedleConfiguration.RESOURCE_JBEHAVE_NEEDLE));
		if (injectionProviders != null) {
			addInjectionProvider(toArray(injectionProviders));
		}
		if (this.configuration == null) {
			this.configuration = new MostUsefulConfiguration();
		} else {
			this.configuration = configuration;
		}
		this.steps = steps;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CandidateSteps> createCandidateSteps() {
		final List<CandidateSteps> result = new ArrayList<CandidateSteps>();
		for (final Class<?> type : steps) {
			if (hasAnnotatedMethods(type)) {
				configuration.parameterConverters().addConverters(methodReturningConverters(type));
				result.add(new Steps(configuration, type, this));
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object createInstanceOfType(Class<?> type) {
		final Object instance = cachedStepsInstances.get(type);
		if (instance == null) {
			if (logger.isTraceEnabled()) {
				logger.debug("createInstanceOfType(): " + type.getCanonicalName());
			}
			try {
				final Object stepsInstance = CreateInstanceByDefaultConstructor.INSTANCE.apply(type);
				final InjectionProvider<?>[] foundProviders = CollectInjectionProvidersFromStepsInstance.INSTANCE
						.apply(stepsInstance);
				addInjectionProvider(foundProviders);
				initTestcase(stepsInstance);
				cachedStepsInstances.put(type, stepsInstance);
				return stepsInstance;
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return instance;
	}

	/**
	 * Create parameter converters from methods annotated with @AsParameterConverter
	 * 
	 * @see {@link AbstractStepsFactory}
	 */
	private List<ParameterConverter> methodReturningConverters(final Class<?> type) {
		final List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
		for (final Method method : type.getMethods()) {
			if (method.isAnnotationPresent(AsParameterConverter.class)) {
				converters.add(new MethodReturningConverter(method, type, this));
			}
		}
		return converters;
	}

	/**
	 * Add injection providers.
	 * 
	 * @param providers
	 *            add injection providers after factory construction.
	 */
	public void addInjectionProviders(final Set<InjectionProvider<?>> providers) {
		if (providers != null) {
			addInjectionProvider(toArray(providers));
		}
	}

	/**
	 * Determines if the given type is a {@link Class} containing at least one method annotated with annotations from
	 * package "org.jbehave.core.annotations".
	 * 
	 * @param type
	 *            the Type of the steps instance
	 * @return A boolean, <code>true</code> if at least one annotated method is found.
	 * @see {@link AbstractStepsFactory}
	 */
	static boolean hasAnnotatedMethods(final Type type) {
		if (type instanceof Class<?>) {
			for (final Method method : ((Class<?>) type).getMethods()) {
				for (final Annotation annotation : method.getAnnotations()) {
					if (annotation.annotationType().getName().startsWith("org.jbehave.core.annotations")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Read injection providers configuration from a resource.
	 * 
	 * @param resourceName resource name
	 * @return injection providers.
	 */
	static InjectionProvider<?>[] setUpInjectionProviders(final String resourceName) {
		return new JBehaveNeedleConfiguration(resourceName).getInjectionProviders();
	}

	/**
	 * Set to array.
	 * 
	 * @param injectionProviders
	 *            set of providers
	 * @return array of providers
	 */
	static InjectionProvider<?>[] toArray(final Set<InjectionProvider<?>> injectionProviders) {
		return injectionProviders.toArray(new InjectionProvider<?>[injectionProviders.size()]);
	}

}
