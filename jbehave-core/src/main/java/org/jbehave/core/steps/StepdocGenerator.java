package org.jbehave.core.steps;

import java.util.List;

/**
 *  Generates a list of {@link Stepdoc}s from the annotations of the given {@link CandidateSteps} instances.
 */
public interface StepdocGenerator {

	List<Stepdoc> generate(CandidateSteps... steps);

}
