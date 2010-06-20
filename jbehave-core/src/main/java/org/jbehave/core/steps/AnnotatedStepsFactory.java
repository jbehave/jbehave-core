package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.configuration.AnnotatedConfigurationBuilder;

public class AnnotatedStepsFactory implements InjectableStepsFactory {

	private Object annotatedRunner;

	public AnnotatedStepsFactory(Object pAnnotatedRunner) {

		annotatedRunner = pAnnotatedRunner;
	}

	public List<CandidateSteps> createCandidateSteps() {
		
		List<CandidateSteps> candidateSteps = AnnotatedConfigurationBuilder.buildCandidateSteps(annotatedRunner);
		return candidateSteps;
	}

}
