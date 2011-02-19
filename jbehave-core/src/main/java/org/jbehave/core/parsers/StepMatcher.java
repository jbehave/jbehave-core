package org.jbehave.core.parsers;

import org.jbehave.core.model.StepPattern;

/**
 * A step matcher is responsible for matching steps against a given step pattern
 * and extracting the parameters for the step
 */
public interface StepMatcher {

    boolean matches(String stepWithoutStartingWord);

    boolean find(String stepWithoutStartingWord);

    String parameter(int matchedPosition);

    String[] parameterNames();

    StepPattern pattern();

}