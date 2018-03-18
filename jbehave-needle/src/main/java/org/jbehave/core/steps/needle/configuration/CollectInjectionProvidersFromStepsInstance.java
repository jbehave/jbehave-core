package org.jbehave.core.steps.needle.configuration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jbehave.core.annotations.needle.NeedleInjectionProvider;
import org.needle4j.injection.InjectionProvider;
import org.needle4j.injection.InjectionProviderInstancesSupplier;
import org.needle4j.reflection.ReflectionUtil;

/**
 * Collects {@link InjectionProvider} instances.
 * 
 * @author Jan Galinski, Holisticon AG (jan.galinski@holisticon.de)
 * @author Simon Zambrovski, Holisticon AG (simon.zambrovski@holisticon.de)
 */
public enum CollectInjectionProvidersFromStepsInstance {
	/**
	 * stateless Singleton
	 */
	INSTANCE;

	/**
	 * Collect providers direct in the step definition.
	 * 
	 * @param instance
	 *            step definition instance
	 * @return collected injection providers.
	 */
	public final <T> InjectionProvider<?>[] apply(final T instance) {
		final Set<InjectionProvider<?>> providers = new LinkedHashSet<>();
		for (final Field field : ReflectionUtil.getAllFieldsWithAnnotation(instance, NeedleInjectionProvider.class)) {
			field.setAccessible(true);
			try {
				final Object value = field.get(instance);
				if (value instanceof InjectionProvider<?>[]) {
					providers.addAll(Arrays.asList((InjectionProvider<?>[]) value));
				} else if (value instanceof InjectionProvider) {
					providers.add((InjectionProvider<?>) value);
				} else if (value instanceof InjectionProviderInstancesSupplier) {
					providers.addAll(((InjectionProviderInstancesSupplier) value).get());
				} else {
					throw new IllegalStateException("Fields annotated with NeedleInjectionProviders must be of type "
							+ "InjectionProviderInstancesSupplier, InjectionProvider " + "or InjectionProvider[]");
				}
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
		}

		return providers.toArray(new InjectionProvider<?>[providers.size()]);
	}

}
