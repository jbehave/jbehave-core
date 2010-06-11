package org.jbehave.core.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class UnmodifiableEmbedderConfiguration extends EmbedderConfiguration {

	private EmbedderConfiguration delegate;

	public UnmodifiableEmbedderConfiguration(EmbedderConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean batch() {
		return delegate.batch();
	}

	@Override
	public boolean ignoreFailureInReports() {
		return delegate.ignoreFailureInReports();
	}

	@Override
	public boolean ignoreFailureInStories() {
		return delegate.ignoreFailureInStories();
	}

	@Override
	public boolean renderReportsAfterStories() {
		return delegate.renderReportsAfterStories();
	}

	@Override
	public boolean skip() {
		return delegate.skip();
	}

	@Override
	public EmbedderConfiguration doBatch(boolean batch) {
		throw notAllowed();
	}

	@Override
	public EmbedderConfiguration doIgnoreFailureInReports(
			boolean ignoreFailureInReports) {
		throw notAllowed();
	}

	@Override
	public EmbedderConfiguration doIgnoreFailureInStories(
			boolean ignoreFailureInStories) {
		throw notAllowed();
	}

	@Override
	public EmbedderConfiguration doRenderReportsAfterStories(
			boolean renderReportsAfterStories) {
		throw notAllowed();
	}

	@Override
	public EmbedderConfiguration doSkip(boolean skip) {
		throw notAllowed();
	}

	private RuntimeException notAllowed() {
		return new RuntimeException("Configuration elements are unmodifiable");
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append(delegate).toString();
	}

}
