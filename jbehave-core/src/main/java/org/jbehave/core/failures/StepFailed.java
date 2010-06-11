package org.jbehave.core.failures;

import org.jbehave.core.model.OutcomesTable;

/**
 * Thrown when a step execution has failed
 */
@SuppressWarnings("serial")
public class StepFailed extends RuntimeException {

	public StepFailed(String step, Throwable cause) {
		super("'" + step + "': "+cause.getMessage());
		initCause(cause);
	}

	public StepFailed(String step, OutcomesTable table) {
		super("'" + step + "': "+table);
		initCause(table.failureCause());
	}

}
