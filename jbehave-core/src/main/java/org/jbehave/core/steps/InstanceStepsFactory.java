package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;

/**
 * Factory class to create {@link CandidateSteps} from Object instances. The
 * factory allows candidate steps methods to be defined in any Object instances
 * and wrapped by {@link Steps} rather than having to extend {@link Steps}. Both
 * "has-a" relationship and "is-a" design models are thus supported.
 */
public class InstanceStepsFactory implements InjectableStepsFactory {

	private final Configuration configuration;
	private final Object[] stepsInstances;

	public InstanceStepsFactory(Object... stepsInstances) {
		this(new MostUsefulConfiguration(), stepsInstances);
	}

	public InstanceStepsFactory(Configuration configuration,
			Object... stepsInstances) {
		this.configuration = configuration;
		this.stepsInstances = stepsInstances;
	}

	public List<CandidateSteps> createCandidateSteps() {
		List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
		for (int i = 0; i < stepsInstances.length; i++) {
			candidateSteps.add(new Steps(configuration, stepsInstances[i]));
		}
		return candidateSteps;
	}
}
