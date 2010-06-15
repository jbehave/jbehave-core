package org.jbehave.core.steps;

import java.util.List;

public interface InjectableStepsFactory {

	List<CandidateSteps> createCandidateSteps();

}