package org.jbehave.core.steps;

/**
 * Enum representing the step types
 */
public enum StepType {

	/** 
	 * Represents a precondition to an event
	 */
	GIVEN, 
	
	/**
	 * Represents an event
	 */
	WHEN, 
	
	/**
	 * Represents an outcome of an event
	 */
	THEN,
	
    /**
     * Represents repetition of previous step
     */
	AND,
	
	/**
	 * Represents an ignorable step 
	 */
	IGNORABLE
}
