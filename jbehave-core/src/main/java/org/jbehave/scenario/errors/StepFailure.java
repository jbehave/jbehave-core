package org.jbehave.scenario.errors;

import org.jbehave.scenario.steps.Step;

/**
 * Indicates the {@link Step} during which a failure occurred. 
 */
@SuppressWarnings("serial")
public class StepFailure extends RuntimeException {

	public StepFailure(String stepAsString, Throwable cause) {
		super(cause.getMessage() + "\nduring step: '" + stepAsString + "'");
		this.initCause(cause);
	}

}
