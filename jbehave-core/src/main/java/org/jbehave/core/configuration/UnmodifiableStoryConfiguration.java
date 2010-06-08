package org.jbehave.core.configuration;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector;

/**
 * Decorator of StoryConfiguration that disables modification of configuration elements.
 */
public class UnmodifiableStoryConfiguration extends StoryConfiguration {

    private final StoryConfiguration delegate;

    public UnmodifiableStoryConfiguration() {
        this(new MostUsefulStoryConfiguration());
    }

    public UnmodifiableStoryConfiguration(StoryConfiguration delegate) {
        this.delegate = delegate;
    }

    public StoryReporter storyReporter() {
       return delegate.storyReporter();
    }

    public StoryParser storyParser() {
        return delegate.storyParser();
    }

    public PendingErrorStrategy pendingErrorStrategy() {
        return delegate.pendingErrorStrategy();
    }

    public StepCollector stepCollector() {
        return delegate.stepCollector();
    }

    public ErrorStrategy errorStrategy() {
        return delegate.errorStrategy();
    }

    public Keywords keywords() {
        return delegate.keywords();
    }

    @Override
    public StoryConfiguration useKeywords(Keywords keywords) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useStepCollector(StepCollector stepCollector) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration usePendingErrorStrategy(PendingErrorStrategy pendingErrorStrategy) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useErrorStrategy(ErrorStrategy errorStrategy) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useStoryParser(StoryParser storyParser) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useStoryReporter(StoryReporter storyReporter) {
        throw notAllowed();
    }

    private RuntimeException notAllowed() {
        return new RuntimeException("Configuration elements are unmodifiable");
    }
}