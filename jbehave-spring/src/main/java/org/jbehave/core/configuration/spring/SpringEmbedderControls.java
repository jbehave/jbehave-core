package org.jbehave.core.configuration.spring;

import org.jbehave.core.embedder.EmbedderControls;

/**
 * Extends {@link EmbedderControls} to provide getter/setter methods for all
 * control properties, so it can be used by Spring's property mechanism.
 *
 * @author Valery Yatsynovich
 */
public class SpringEmbedderControls extends EmbedderControls {

    public boolean isBatch() {
        return batch();
    }

    public void setBatch(boolean batch) {
        doBatch(batch);
    }

    public boolean isSkip() {
        return skip();
    }

    public void setSkip(boolean skip) {
        doSkip(skip);
    }

    public boolean isGenerateViewAfterStories() {
        return generateViewAfterStories();
    }

    public void setGenerateViewAfterStories(boolean generateViewAfterStories) {
        doGenerateViewAfterStories(generateViewAfterStories);
    }

    public boolean isIgnoreFailureInStories() {
        return ignoreFailureInStories();
    }

    public void setIgnoreFailureInStories(boolean ignoreFailureInStories) {
        doIgnoreFailureInStories(ignoreFailureInStories);
    }

    public boolean isIgnoreFailureInView() {
        return ignoreFailureInView();
    }

    public void setIgnoreFailureInView(boolean ignoreFailureInView) {
        doIgnoreFailureInView(ignoreFailureInView);
    }

    public boolean isVerboseFailures() {
        return verboseFailures();
    }

    public void setVerboseFailures(boolean verboseFailures) {
        doVerboseFailures(verboseFailures);
    }

    public boolean isVerboseFiltering() {
        return verboseFiltering();
    }

    public void setVerboseFiltering(boolean verboseFiltering) {
        doVerboseFiltering(verboseFiltering);
    }

    public String getStoryTimeouts() {
        return storyTimeouts();
    }

    public void setStoryTimeouts(String storyTimeouts) {
        useStoryTimeouts(storyTimeouts);
    }

    public boolean isFailOnStoryTimeout() {
        return failOnStoryTimeout();
    }

    public void setFailOnStoryTimeout(boolean failOnStoryTimeout) {
        doFailOnStoryTimeout(failOnStoryTimeout);
    }

    public int getThreads() {
        return threads();
    }

    public void setThreads(int threads) {
        useThreads(threads);
    }
}
