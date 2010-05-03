package org.jbehave.core.parser;

import java.util.regex.Pattern;

/**
 * Builds a regex pattern from a template step, as provided in the annotations,
 * which will in turn match real steps conforming to the template. Eg: &quot;I
 * give $money to $name&quot; becomes &quot;I give (.*) to (.*)&quot;, which matches
 * &quot;I give £10 to Fred&quot;. The captured arguments will be &quot;£10&quot; 
 * and &quot;Fred&quot;.</p>
 * 
 * <p>To create your own pattern builder, the text in the annotation should
 * be converted to a pattern that matches a real step in the core with
 * any precursor words removed. The arguments in the real step should
 * be the only captured groups.</p>
 */
public interface StepPatternBuilder {

    /**
     * Builds a regexp pattern from a template step.
     * @param matchThis the template step
     * @return a regexp pattern which will capture the arguments associated with a matching real step
     */
    Pattern buildPattern(String matchThis);

    /**
     * Extract the parameter names from a template step
     * @param step the template step
     * @return an array of parameter names
     */
	String[] extractGroupNames(String string);

}
