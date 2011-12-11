package org.jbehave.core.embedder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holds values used by the Embedder to control execution flow.
 */
public class EmbedderControls {

    private boolean batch = false;
    private boolean skip = false;
    private boolean generateViewAfterStories = true;
    private boolean ignoreFailureInStories = false;
    private boolean ignoreFailureInView = false;
    private boolean verboseFailures = false;
    private long storyTimeoutInSecs = 300; // 5 mins is default.
    private int threads = 1;

    public EmbedderControls() {
    }

    public boolean batch() {
        return batch;
    }

    public boolean skip() {
        return skip;
    }

    public boolean generateViewAfterStories() {
        return generateViewAfterStories;
    }

    public boolean ignoreFailureInStories() {
        return ignoreFailureInStories;
    }

    public boolean ignoreFailureInView() {
        return ignoreFailureInView;
    }

    public boolean verboseFailures(){
        return verboseFailures;
    }
    
    public long storyTimeoutInSecs() {
        return storyTimeoutInSecs;
    }

    public int threads() {
       return threads;
    }

    public EmbedderControls doBatch(boolean batch) {
        this.batch = batch;
        return this;
    }

    public EmbedderControls doSkip(boolean skip) {
        this.skip = skip;
        return this;
    }

    public EmbedderControls doGenerateViewAfterStories(boolean generateViewAfterStories) {
        this.generateViewAfterStories = generateViewAfterStories;
        return this;
    }

    public EmbedderControls doIgnoreFailureInStories(boolean ignoreFailureInStories) {
        this.ignoreFailureInStories = ignoreFailureInStories;
        return this;
    }

    public EmbedderControls doIgnoreFailureInView(boolean ignoreFailureInView) {
        this.ignoreFailureInView = ignoreFailureInView;
        return this;
    }

    public EmbedderControls doVerboseFailures(boolean verboseFailures){
        this.verboseFailures = verboseFailures;
        return this;        
    }
    
    public EmbedderControls useStoryTimeoutInSecs(long storyTimeoutInSecs) {
        this.storyTimeoutInSecs = storyTimeoutInSecs;
        return this;
    }

    public EmbedderControls useThreads(int threads) {
        this.threads = threads;
        return this;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
