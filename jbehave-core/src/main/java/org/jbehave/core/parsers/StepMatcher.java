package org.jbehave.core.parsers;

public interface StepMatcher {

	boolean matches(String stepWithoutStartingWord);

	boolean find(String stepWithoutStartingWord);

	String parameter(int i);

	String[] parameterNames();

	String pattern();

}