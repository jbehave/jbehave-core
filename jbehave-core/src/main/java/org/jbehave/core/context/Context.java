package org.jbehave.core.context;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Holds context-related information
 */
public class Context {
	
	private ThreadLocal<String> currentStory = new ThreadLocal<>();
	private ThreadLocal<String> currentScenario = new ThreadLocal<>();
	private ThreadLocal<List<String>> currentSteps = new ThreadLocal<>();

    public String getCurrentStory() {
		return currentStory.get();
	}

	public void setCurrentStory(String currentStory) {
		this.currentStory.set(currentStory);
	}

	public String getCurrentScenario() {
		return currentScenario.get();
	}

	public void setCurrentScenario(String currentScenario) {
		this.currentScenario.set(currentScenario);
	}

	public List<String> getCurrentSteps() {
		return currentSteps.get();
	}

	public void setCurrentSteps(List<String> currentSteps) {
		this.currentSteps.set(currentSteps);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("story="+getCurrentStory())
				.append("scenario="+getCurrentScenario())
				.append("steps="+getCurrentSteps())
				.build();
	}

}
