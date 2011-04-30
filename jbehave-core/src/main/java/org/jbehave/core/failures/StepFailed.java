package org.jbehave.core.failures;

import org.jbehave.core.model.OutcomesTable;

/**
 * Thrown when a step execution has failed
 */
@SuppressWarnings("serial")
public class StepFailed extends UUIDExceptionWrapper {

	public StepFailed(String step, Throwable cause) {
		super("'" + step + "': " + cause.getMessage(), cause);
	}

	public StepFailed(String step, OutcomesTable table) {
		super("'" + step + "': "+table, table.failureCause());
	}

}
