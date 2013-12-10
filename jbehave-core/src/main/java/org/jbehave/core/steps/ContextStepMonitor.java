package org.jbehave.core.steps;

import org.jbehave.core.context.ContextView;
import org.jbehave.core.context.Context;

/**
 * Decorator of {@link StepMonitor} which shows the current context via the
 * {@link ContextView}.
 */
public class ContextStepMonitor extends DelegatingStepMonitor {

	private final Context context;
	private final ContextView view;

	public ContextStepMonitor(Context context, ContextView view,
			StepMonitor delegate) {
		super(delegate);
		this.context = context;
		this.view = view;
	}

	public void performing(String step, boolean dryRun) {
		String currentScenario = context.getCurrentScenario();
		view.show(currentScenario, step);
		super.performing(step, dryRun);
	}

}