package org.jbehave.core.errors;

import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.steps.Step;

/**
 * Indicates the {@link Step} during which a failure occurred. 
 */
@SuppressWarnings("serial")
public class StepFailure extends RuntimeException {

	public StepFailure(String step, Throwable cause) {
		super("Failure during step: '" + step + "': "+cause.getMessage());
		initCause(cause);
	}

	public StepFailure(String step, OutcomesTable table) {
		super("Outcome failures during step: '" + step + "': "+table);
		initCause(table.failureCause());
	}

}
