package org.jbehave.core;

public class StoryRunnerMode {
    private boolean batch;
    private boolean skip;
    private boolean ignoreFailure;

    public StoryRunnerMode() {
        this(false, false, false);
    }
    
    public StoryRunnerMode(boolean batch, boolean skip, boolean ignoreFailure) {
        this.batch = batch;
        this.skip = skip;
        this.ignoreFailure = ignoreFailure;
    }

    public boolean ignoreFailure() {
        return ignoreFailure;
    }

    public boolean batch() {
        return batch;
    }

    public boolean skip() {
        return skip;
    }
}
