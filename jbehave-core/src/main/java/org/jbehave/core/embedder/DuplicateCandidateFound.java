package org.jbehave.core.embedder;

import static java.text.MessageFormat.format;

import org.jbehave.core.steps.StepCandidate;

public class DuplicateCandidateFound extends RuntimeException {

    private static final long serialVersionUID = -4210169420292605056L;

    private static final String DUPLICATE_FORMAT = "{0} {1}";

    public DuplicateCandidateFound(StepCandidate candidate) {
        super(format(DUPLICATE_FORMAT, candidate.getStepType(), candidate.getPatternAsString()));
    }

    public DuplicateCandidateFound(String step) {
        super(step);
    }

}
