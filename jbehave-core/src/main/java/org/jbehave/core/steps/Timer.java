package org.jbehave.core.steps;

public class Timer {
    private long start;

    public Timer start() {
        start = System.currentTimeMillis();
        return this;
    }

    /**
     * @return the milliseconds elapsed since the timer was started, or zero if the timer was never started
     */
    public long stop() {
        if (start == 0)
            return 0;
        return System.currentTimeMillis() - start;
    }
}