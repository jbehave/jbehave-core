package org.jbehave.core.steps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;

/**
 * <p>
 * An abstract implementation of {@link InjectableStepsFactory} that is provided
 * by concrete subclasses Object instances which contain the candidate steps
 * methods. The Object instances are wrapped by {@link Steps}.
 * </p>
 * <p>
 * The object instances are also inspected for methods annotated by {@link AsParameterConverter}
 * and the {@link ParameterConverter} is configured accordingly.
 * </p>
 */
public abstract class AbstractStepsFactory implements InjectableStepsFactory {

	private final Configuration configuration;

	public AbstractStepsFactory(Configuration configuration) {
		this.configuration = configuration;
	}

	public List<CandidateSteps> createCandidateSteps() {
		List<Object> stepsInstances = stepsInstances();
		List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
		for (Object instance : stepsInstances) {
			configuration.parameterConverters().addConverters(
					methodReturningConverters(instance));
			steps.add(new Steps(configuration, instance));
		}
		return steps;
	}

	protected abstract List<Object> stepsInstances();

	/**
	 * Create parameter converters from methods annotated with @AsParameterConverter
	 */
	private List<ParameterConverter> methodReturningConverters(Object instance) {
		List<ParameterConverter> converters = new ArrayList<ParameterConverter>();

		for (Method method : instance.getClass().getMethods()) {
			if (method.isAnnotationPresent(AsParameterConverter.class)) {
				converters.add(new MethodReturningConverter(method, instance));
			}
		}

		return converters;
	}

	/**
	 * Determines if the given type is a {@link Class} containing at least one method 
     * annotated with annotations from package "org.jbehave.core.annotations".
	 * 
	 * @param type the Type of the steps instance
	 * @return A boolean, <code>true</code> if at least one annotated method is found.
	 */
    protected boolean hasAnnotatedMethods(Type type) {
		if (type instanceof Class<?>) {
			for (Method method : ((Class<?>) type).getMethods()) {
				for (Annotation annotation : method.getAnnotations()) {
					if (annotation.annotationType().getName().startsWith(
							"org.jbehave.core.annotations")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
