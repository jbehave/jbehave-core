package org.jbehave.core.steps;

/**
 * Decorator of {@link StepMonitor} which shows the current context via the
 * {@link ContextView}.
 */
public class ContextStepMonitor extends DelegatingStepMonitor {

	private final ContextView view;
	private final StepContext context;

	public ContextStepMonitor(ContextView view, StepContext context,
			StepMonitor delegate) {
		super(delegate);
		this.view = view;
		this.context = context;
	}

	public void performing(String step, boolean dryRun) {
		String currentScenario = context.getCurrentScenario();
		view.show(currentScenario, step);
		super.performing(step, dryRun);
	}

}