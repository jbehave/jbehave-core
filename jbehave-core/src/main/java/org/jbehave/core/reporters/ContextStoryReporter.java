package org.jbehave.core.reporters;

import org.jbehave.core.context.Context;

public class ContextStoryReporter extends NullStoryReporter {
	private final Context context;

	public ContextStoryReporter(Context context) {
		this.context = context;
	}

	@Override
	public void beforeScenario(String title) {
		context.setCurrentScenario(title);
	}
}
