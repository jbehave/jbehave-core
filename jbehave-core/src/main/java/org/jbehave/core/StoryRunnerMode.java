package org.jbehave.core;

public class StoryRunnerMode {
    private boolean batch;
    private boolean skip;
    private boolean ignoreFailureInStories;
	private boolean ignoreFailureInReports;
	private boolean renderReportsAfterStories;

    public StoryRunnerMode() {
        this(false, false, false, false, true);
    }
    
    public StoryRunnerMode(boolean batch, boolean skip, boolean ignoreFailureInStories, boolean ignoreFailureInReports, boolean renderReportsAfterStories) {
        this.batch = batch;
        this.skip = skip;
        this.ignoreFailureInStories = ignoreFailureInStories;
		this.ignoreFailureInReports = ignoreFailureInReports;
		this.renderReportsAfterStories = renderReportsAfterStories;
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
}
