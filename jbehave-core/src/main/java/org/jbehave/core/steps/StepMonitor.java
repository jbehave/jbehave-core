package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Interface to monitor step events
 * 
 * @author Mauro Talevi
 */
public interface StepMonitor {

	void stepMatchesType(String stepAsString, String previousAsString,
			boolean matchesType, StepType stepType, Method method, Object stepsInstance);

	void stepMatchesPattern(String step, boolean matches, String pattern,
			Method method, Object stepsInstance);

    void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass);

    void performing(String step, boolean dryRun);

	void usingAnnotatedNameForArg(String name, int position);

	void usingParameterNameForArg(String name, int position);

	void usingTableAnnotatedNameForArg(String name, int position);

	void usingTableParameterNameForArg(String name, int position);

	void usingNaturalOrderForArg(int position);

	void foundArg(String arg, int position);
}
