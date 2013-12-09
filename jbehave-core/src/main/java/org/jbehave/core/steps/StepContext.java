package org.jbehave.core.steps;

/**
 * Holds context-related information
 */
public class StepContext {
	
	private ThreadLocal<String> currentScenario = new ThreadLocal<String>();

    public String getCurrentScenario() {
		return currentScenario.get();
	}

	public void setCurrentScenario(String currentScenario) {
		this.currentScenario.set(currentScenario);
	}

}
