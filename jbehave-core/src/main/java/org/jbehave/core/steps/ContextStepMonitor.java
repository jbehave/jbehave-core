package org.jbehave.core.steps;

import java.lang.reflect.Method;

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
			StepMonitor... delegates) {
		super(delegates);
		this.context = context;
		this.view = view;
	}

	@Override
    public void beforePerforming(String step, boolean dryRun, Method method) {
		String currentStory = context.getCurrentStory();
		String currentScenario = context.getCurrentScenario();
		view.show(currentStory, currentScenario, step);
		super.beforePerforming(step, dryRun, method);
	}

}
