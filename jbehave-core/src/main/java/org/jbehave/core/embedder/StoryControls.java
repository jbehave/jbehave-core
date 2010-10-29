package org.jbehave.core.embedder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holds flags used by the StoryRunner to control story execution flow.
 */
public class StoryControls {
	
	private boolean dryRun = false;
	private boolean skipScenariosAfterFailure = false;

	public StoryControls() {
	}

	public boolean dryRun() {
		return dryRun;
	}

	public boolean skipScenariosAfterFailure() {
		return skipScenariosAfterFailure;
	}

	public StoryControls doDryRun(boolean dryRun) {
		this.dryRun = dryRun;
		return this;
	}

	public StoryControls doSkipScenariosAfterFailure(boolean skipScenariosAfterFailure) {
		this.skipScenariosAfterFailure = skipScenariosAfterFailure;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
