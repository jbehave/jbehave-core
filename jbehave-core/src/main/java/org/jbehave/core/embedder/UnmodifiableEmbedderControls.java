package org.jbehave.core.embedder;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UnmodifiableEmbedderControls extends EmbedderControls {

    private EmbedderControls delegate;

    public UnmodifiableEmbedderControls(EmbedderControls delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean batch() {
        return delegate.batch();
    }

    @Override
    public boolean ignoreFailureInView() {
        return delegate.ignoreFailureInView();
    }

    @Override
    public boolean ignoreFailureInStories() {
        return delegate.ignoreFailureInStories();
    }

    @Override
    public boolean generateViewAfterStories() {
        return delegate.generateViewAfterStories();
    }

    @Override
    public boolean skip() {
        return delegate.skip();
    }

    @Override
    public boolean verboseFailures() {
        return delegate.verboseFailures();
    }

    @Override
    public boolean verboseFiltering() {
        return delegate.verboseFiltering();
    }

    @Override
    public String storyTimeouts() {
        return delegate.storyTimeouts();
    }
    
    @Override
    public boolean failOnStoryTimeout() {
        return delegate.failOnStoryTimeout();
    }

    @Override
    public int threads() {
        return delegate.threads();
    }

    @Override
    public EmbedderControls doBatch(boolean batch) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls doIgnoreFailureInView(boolean ignoreFailureInReports) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls doIgnoreFailureInStories(boolean ignoreFailureInStories) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls doGenerateViewAfterStories(boolean generateViewAfterStories) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls doSkip(boolean skip) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls doVerboseFailures(boolean verboseFailures) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls doVerboseFiltering(boolean verboseFiltering) {
        throw notAllowed();
    }

    @Override
    public EmbedderControls useThreads(int threads) {
        throw notAllowed();
    }

    private RuntimeException notAllowed() {
        return new ModificationNotAllowed();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(delegate).toString();
    }

    @SuppressWarnings("serial")
    public static class ModificationNotAllowed extends RuntimeException {
        public ModificationNotAllowed() {
            super("Configuration elements are unmodifiable");
        }
    }

}
