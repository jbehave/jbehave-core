package org.jbehave.core.steps;

import java.util.List;

/**
 * Interface abstracting the creation of {@link CandidateSteps}.
 * Concrete implementations will provide different mechanisms
 * to create the steps instances.
 */
public interface InjectableStepsFactory {

    List<CandidateSteps> createCandidateSteps();

    Object createInstanceOfType(Class<?> type);

}