package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.configuration.Configuration;

/**
 * Interface abstracting the creation of {@link CandidateSteps}.
 * Concrete implementations should be injected with the mechanism
 * to instantiate the CandidateSteps instances.
 */
public interface InjectableStepsFactory {

    Configuration getConfiguration();
    
	List<CandidateSteps> createCandidateSteps();

}