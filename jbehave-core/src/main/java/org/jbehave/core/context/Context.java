package org.jbehave.core.context;

/**
 * Holds context-related information
 */
public class Context {
	
	private ThreadLocal<String> currentScenario = new ThreadLocal<String>();

    public String getCurrentScenario() {
		return currentScenario.get();
	}

	public void setCurrentScenario(String currentScenario) {
		this.currentScenario.set(currentScenario);
	}

}
