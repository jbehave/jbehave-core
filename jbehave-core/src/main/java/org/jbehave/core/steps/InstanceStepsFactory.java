package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;


/**
 * An {@link InjectableStepsFactory} that is provided Object instances, which
 * contain the candidate steps methods. The Object instances are wrapped by
 * {@link Steps}. The use of this factory allows steps classes to not have to
 * extend {@link Steps}.
 */
public class InstanceStepsFactory implements InjectableStepsFactory {

	private final Configuration configuration;
	private final Object[] stepsInstances;

	public InstanceStepsFactory(Configuration configuration,
			Object... stepsInstances) {
		this.configuration = configuration;
		this.stepsInstances = stepsInstances;
	}

	public List<CandidateSteps> createCandidateSteps() {
		List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
		for (Object instance : stepsInstances) {
			configuration.parameterConverters().addConverters(methodReturningConverters(instance));
			steps.add(new Steps(configuration, instance));
		}
		return steps;
	}


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

}
