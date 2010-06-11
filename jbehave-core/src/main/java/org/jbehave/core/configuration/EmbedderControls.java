package org.jbehave.core.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class EmbedderControls {
	
	private boolean batch = false;
	private boolean skip = false;
	private boolean ignoreFailureInStories = false;
	private boolean ignoreFailureInReports = false;
	private boolean renderReportsAfterStories = true;

	public EmbedderControls() {
	}

	public boolean batch() {
		return batch;
	}

	public boolean skip() {
		return skip;
	}

	public boolean ignoreFailureInStories() {
		return ignoreFailureInStories;
	}

	public boolean ignoreFailureInReports() {
		return ignoreFailureInReports;
	}

	public boolean renderReportsAfterStories() {
		return renderReportsAfterStories;
	}

	public EmbedderControls doBatch(boolean batch) {
		this.batch = batch;
		return this;
	}

	public EmbedderControls doSkip(boolean skip) {
		this.skip = skip;
		return this;
	}

	public EmbedderControls doIgnoreFailureInStories(boolean ignoreFailureInStories) {
		this.ignoreFailureInStories = ignoreFailureInStories;
		return this;
	}

	public EmbedderControls doIgnoreFailureInReports(boolean ignoreFailureInReports) {
		this.ignoreFailureInReports = ignoreFailureInReports;
		return this;
	}

	public EmbedderControls doRenderReportsAfterStories(boolean renderReportsAfterStories) {
		this.renderReportsAfterStories = renderReportsAfterStories;
		return this;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
