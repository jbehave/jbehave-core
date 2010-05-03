package org.jbehave.core.steps;

import java.lang.reflect.Type;

/**
 * Interface to monitor step events
 * 
 * @author Mauro Talevi
 */
public interface StepMonitor {

	void stepMatchesType(String stepAsString, String previousAsString,
			boolean matchesType, StepType stepType);

    void stepMatchesPattern(String step, boolean matches, String pattern);

    void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass);

    void performing(String step);

	void usingAnnotatedNameForArg(String name, int position);

	void usingParameterNameForArg(String name, int position);

	void usingTableAnnotatedNameForArg(String name, int position);

	void usingTableParameterNameForArg(String name, int position);

	void usingNaturalOrderForArg(int position);

	void foundArg(String arg, int position);
}
