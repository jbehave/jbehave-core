package org.jbehave.core.steps;

import java.util.List;

/**
 * Defines the priorising strategy pf candidate steps
 */
public interface PrioritisingStrategy {

    List<CandidateStep> prioritise(String stepAsString, List<CandidateStep> candidateSteps);
}
