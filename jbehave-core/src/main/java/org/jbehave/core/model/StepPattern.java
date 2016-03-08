package org.jbehave.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.parsers.RegexStepMatcher;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.StepType;

/**
 * <p>
 * Represents a step pattern, as provided in the method annotations.  
 * This pattern will in turn be resolved by the chosen {@link StepMatcher},
 * e.g. a regex pattern if using the {@link RegexStepMatcher}
 * </p>
 */
public class StepPattern {

    private StepType stepType;
    private final String annotated;
    private final String resolved;
    
    public StepPattern(StepType stepType, String annotated, String resolved) {
        this.stepType = stepType;
        this.annotated = annotated;
        this.resolved = resolved;
    }

    /**
     * Returns the step pattern as provided in the method annotation
     * @return The String representing the annotated pattern
     */
    public String annotated(){
        return annotated;	    
	}
	
	/**
	 * Return the step pattern as resolved by the step matcher
	 * @return The String representing the resolved pattern
	 */
	public String resolved(){
	    return resolved;
	}

    /**
     * Return the step type
     * @return The enum for the StepType
     */
    public StepType type() {
        return stepType;
    }

    @Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
