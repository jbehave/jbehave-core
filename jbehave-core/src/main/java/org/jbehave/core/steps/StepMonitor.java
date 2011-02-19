package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Interface to monitor step events
 */
public interface StepMonitor {

    public static class NULL implements StepMonitor {
        public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType, Method method, Object stepsInstance) {
        }

        public void stepMatchesPattern(String step, boolean matches, Pattern pattern, Method method, Object stepsInstance) {
        }

        public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass) {
        }

        public void performing(String step, boolean dryRun) {
        }

        public void usingAnnotatedNameForParameter(String name, int position) {
        }

        public void usingParameterNameForParameter(String name, int position) {
        }

        public void usingTableAnnotatedNameForParameter(String name, int position) {
        }

        public void usingTableParameterNameForParameter(String name, int position) {
        }

        public void usingNaturalOrderForParameter(int position) {
        }

        public void foundParameter(String parameter, int position) {
        }
    }

    public static class Pattern {

        String pattern;
        private String pseudoPattern;

        public Pattern(String pattern, String pseudoPattern) {
            this.pattern = pattern;
            this.pseudoPattern = pseudoPattern;
        }

        public String getPseudoPattern() {
            return pseudoPattern;
        }

        public String getPattern() {
            return pattern;
        }

        public int length() {
            return pattern.length();
        }

        public char charAt(int i) {
            return pattern.charAt(i);
        }

        public CharSequence subSequence(int i, int i1) {
            return pattern.subSequence(i, i1);
        }
    }

	void stepMatchesType(String stepAsString, String previousAsString,
			boolean matchesType, StepType stepType, Method method, Object stepsInstance);

	void stepMatchesPattern(String step, boolean matches, Pattern pattern,
			Method method, Object stepsInstance);

    void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass);

    void performing(String step, boolean dryRun);

	void usingAnnotatedNameForParameter(String name, int position);

	void usingParameterNameForParameter(String name, int position);

	void usingTableAnnotatedNameForParameter(String name, int position);

	void usingTableParameterNameForParameter(String name, int position);

	void usingNaturalOrderForParameter(int position);

	void foundParameter(String parameter, int position);
}
