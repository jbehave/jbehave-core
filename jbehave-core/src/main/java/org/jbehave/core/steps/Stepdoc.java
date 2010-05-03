package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 * A Stepdoc represents the documentation on a single {@link Step}, which
 * includes:
 * <ul>
 * <li>the associated annotation in the {@link Steps} class</li>
 * <li>the pattern to match the candidate step that configured in the annotation
 * </li>
 * <li>the aliases for the step (optional)</li>
 * <li>the matched method inthe {@link Steps} class</li>
 * </ul>
 * 
 * @author Mauro Talevi
 */
public class Stepdoc implements Comparable<Stepdoc> {

	private final Class<? extends Annotation> annotation;
	private final String pattern;
	private final List<String> aliasPatterns;
	private final Method method;
	private final CandidateSteps candidateSteps;
	private Integer priority = 0;

	public Stepdoc(Class<? extends Annotation> annotation, String pattern,
			String[] aliasPatterns, Method method, CandidateSteps candidateSteps) {
		this.annotation = annotation;
		this.pattern = pattern;
		this.aliasPatterns = asList(aliasPatterns);
		this.method = method;
		this.candidateSteps = candidateSteps;
		assignPriority();
	}

	private void assignPriority() {
		if (annotation.equals(Given.class)) {
			priority = 1;
		} else if (annotation.equals(When.class)) {
			priority = 2;
		} else if (annotation.equals(Then.class)) {
			priority = 3;
		}

	}

	public Class<? extends Annotation> getAnnotation() {
		return annotation;
	}

	public String getPattern() {
		return pattern;
	}

	public List<String> getAliasPatterns() {
		return aliasPatterns;
	}
	
	public Method getMethod() {
		return method;
	}
	
    public CandidateSteps getCandidateSteps() {
		return candidateSteps;
	}

	/**
     * Method signature without "public void" prefix
     * @return The method signature in String format
     */
    public String getMethodSignature() {
        String methodSignature = method.toString();
        return methodSignature.replaceFirst("public void ", "");
    }

    @Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Stepdoc pattern=").append(pattern).append(", aliases=")
				.append(aliasPatterns).append(", method=").append(getMethodSignature())
				.append(", candidateSteps=").append(candidateSteps.getClass()).append("]");
		return sb.toString();
	}

	public int compareTo(Stepdoc that) {
        int compare = this.priority.compareTo(that.priority);
        if (compare == 0) {
            compare = this.pattern.compareTo(that.pattern);
        }
        return compare;
	}
}
