package org.jbehave.core.model;

public class StoryDuration {

    private final long durationInSecs;
    private final long timeoutInSecs;

    public StoryDuration(long durationInSecs, long timeoutInSecs) {
        this.durationInSecs = durationInSecs;
        this.timeoutInSecs = timeoutInSecs;
    }

    public long getDurationInSecs() {
        return durationInSecs;
    }

    public long getTimeoutInSecs() {
        return timeoutInSecs;
    }
}