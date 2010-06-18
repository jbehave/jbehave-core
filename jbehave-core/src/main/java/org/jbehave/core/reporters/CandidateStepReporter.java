package org.jbehave.core.reporters;

import java.util.List;

import org.jbehave.core.steps.Stepdoc;

public interface CandidateStepReporter {

	void stepsMatching(String stepAsString, List<Stepdoc> matching, List<Object> stepsIntances);

	void stepdocs(List<Stepdoc> stepdocs);

}
