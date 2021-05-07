package org.jbehave.core.parsers;

import java.util.regex.Matcher;

import org.jbehave.core.model.StepPattern;

/**
 * A step matcher is responsible for matching steps against a given step pattern
 * and extracting the parameters for the step
 */
public interface StepMatcher {

    Matcher matcher(String stepWithoutStartingWord);

    String[] parameterNames();

    StepPattern pattern();
}
