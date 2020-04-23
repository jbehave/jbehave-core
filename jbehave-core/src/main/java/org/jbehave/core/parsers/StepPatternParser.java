package org.jbehave.core.parsers;

import org.jbehave.core.steps.StepType;

/**
 * <p>
 * Parses a step pattern, as provided in the method annotations, creating a
 * {@link StepMatcher} which will in turn match textual steps conforming to the pattern.
 * E.g.:
 * 
 * <pre>
 * I give $money to $name
 * </pre>
 * 
 * will match
 * 
 * <pre>
 * I give �10 to Fred
 * </pre>
 * 
 * and the captured parameters will be &quot;�10&quot; and &quot;Fred&quot;.
 * </p>
 * 
 */
public interface StepPatternParser {

	/**
	 * Parses a step pattern to create a step matcher
	 * 
	 *
     * @param stepType
     * @param stepPattern the step pattern
     * @return A StepMatcher that will capture the parameters associated with a
	 *         step
	 */
	StepMatcher parseStep(StepType stepType, String stepPattern);

	/**
	 * Returns current parameter prefix
	 * 
	 * @return parameter prefix, e.g. <b>$</b>
	 */
	String getPrefix();
}
