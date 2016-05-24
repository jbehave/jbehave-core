package org.jbehave.core.steps;

public class Timer {
    private long start;
    private long end;

    public Timer start() {
        start = System.currentTimeMillis();
        return this;
    }

    /**
     * @return the timer
     */
    public Timer stop() {
        end = System.currentTimeMillis();
        return this;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getDuration() {
        return end - start;
    }
}