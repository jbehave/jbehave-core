package org.jbehave.core.embedder;

class StoryResult {
    private final long durationInMillis;

    public StoryResult(long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }
    
    public long getDurationInMillis() {
        return durationInMillis;
    }
}
