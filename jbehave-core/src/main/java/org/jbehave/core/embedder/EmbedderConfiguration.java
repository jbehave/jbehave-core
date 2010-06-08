package org.jbehave.core.embedder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class EmbedderConfiguration {
	
	private boolean batch = false;
	private boolean skip = false;
	private boolean ignoreFailureInStories = false;
	private boolean ignoreFailureInReports = false;
	private boolean renderReportsAfterStories = true;

	public EmbedderConfiguration() {
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

	public EmbedderConfiguration doBatch(boolean batch) {
		this.batch = batch;
		return this;
	}

	public EmbedderConfiguration doSkip(boolean skip) {
		this.skip = skip;
		return this;
	}

	public EmbedderConfiguration doIgnoreFailureInStories(boolean ignoreFailureInStories) {
		this.ignoreFailureInStories = ignoreFailureInStories;
		return this;
	}

	public EmbedderConfiguration doIgnoreFailureInReports(boolean ignoreFailureInReports) {
		this.ignoreFailureInReports = ignoreFailureInReports;
		return this;
	}

	public EmbedderConfiguration doRenderReportsAfterStories(boolean renderReportsAfterStories) {
		this.renderReportsAfterStories = renderReportsAfterStories;
		return this;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
