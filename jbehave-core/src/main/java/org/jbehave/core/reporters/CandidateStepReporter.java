package org.jbehave.core.reporters;

import java.util.List;

import org.jbehave.core.steps.CandidateStep;

public interface CandidateStepReporter {

	void candidateStepsMatching(String stepAsString, List<CandidateStep> candidateSteps, List<Object> stepsIntances);

}
