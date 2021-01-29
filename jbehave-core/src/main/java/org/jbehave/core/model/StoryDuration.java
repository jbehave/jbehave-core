package org.jbehave.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class StoryDuration {

    private static final int CANCEL_TIMEOUT_RATIO = 20;

	private long startedAtMillis;
    private long durationInMillis;
    private final long timeoutInSecs;
    private final long cancelTimeoutInSecs;

    public StoryDuration(long timeoutInSecs) {
        this(System.currentTimeMillis(), timeoutInSecs);
    }

    public StoryDuration(long startedAtMillis, long timeoutInSecs) {
        this.startedAtMillis = startedAtMillis;
        this.timeoutInSecs = timeoutInSecs;
        this.cancelTimeoutInSecs = timeoutInSecs / CANCEL_TIMEOUT_RATIO;
    }

    public long getDurationInSecs() {
        return durationInMillis / 1000;
    }

    public long getTimeoutInSecs() {
        return timeoutInSecs;
    }

    public StoryDuration setDurationInSecs(long durationInSecs) {
		this.durationInMillis = durationInSecs * 1000;		
		return this;
	}

    public StoryDuration update() {
		this.durationInMillis = elapsedTimeInMillis();		
		return this;
	}

    private long elapsedTimeInMillis() {
        return System.currentTimeMillis() - startedAtMillis;
    }    

	public boolean timedOut() {
		return timeoutInSecs != 0 && getDurationInSecs() > timeoutInSecs;
	}

    public boolean cancelTimedOut() {
        return cancelTimeoutInSecs == 0 || getDurationInSecs() > timeoutInSecs + cancelTimeoutInSecs;
    }

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}
}
