package org.jbehave.core.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class StoryDuration {

	private long start;
    private long durationInMillis;
    private final long timeoutInSecs;

    public StoryDuration(long timeoutInSecs) {
        this.start = System.currentTimeMillis();
        this.timeoutInSecs = timeoutInSecs;
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
        return System.currentTimeMillis() - start;
    }    

	public boolean timedOut() {
		return getDurationInSecs() > timeoutInSecs;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}
}