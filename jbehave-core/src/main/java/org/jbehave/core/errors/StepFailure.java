package org.jbehave.core.errors;

import org.jbehave.core.steps.Step;

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
