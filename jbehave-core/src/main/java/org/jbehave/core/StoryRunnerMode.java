package org.jbehave.core;

public class StoryRunnerMode {
    private boolean batch;
    private boolean skip;
    private boolean ignoreFailure;
	private boolean ignoreFailureInReports;

    public StoryRunnerMode() {
        this(false, false, false, false);
    }
    
    public StoryRunnerMode(boolean batch, boolean skip, boolean ignoreFailure, boolean ignoreFailureInReports) {
        this.batch = batch;
        this.skip = skip;
        this.ignoreFailure = ignoreFailure;
		this.ignoreFailureInReports = ignoreFailureInReports;
    }

    public boolean batch() {
        return batch;
    }

    public boolean skip() {
        return skip;
    }

    public boolean ignoreFailure() {
        return ignoreFailure;
    }

	public boolean ignoreFailureInReports() {
		return ignoreFailureInReports;
	}
}
