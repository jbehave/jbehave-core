package org.jbehave.core;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parser.StoryParser;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepdocGenerator;

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

    public StepCreator stepCreator() {
        return delegate.stepCreator();
    }

    public ErrorStrategy errorStrategy() {
        return delegate.errorStrategy();
    }

    public Keywords keywords() {
        return delegate.keywords();
    }

	public StepdocGenerator stepdocGenerator() {
		return delegate.stepdocGenerator();
	}

	public StepdocReporter stepdocReporter() {
		return delegate.stepdocReporter();
	}

    @Override
    public void useKeywords(Keywords keywords) {
        notAllowed();
    }

    @Override
    public void useStepCreator(StepCreator stepCreator) {
        notAllowed();
    }

    @Override
    public void usePendingErrorStrategy(PendingErrorStrategy pendingErrorStrategy) {
        notAllowed();
    }

    @Override
    public void useErrorStrategy(ErrorStrategy errorStrategy) {
        notAllowed();
    }

    @Override
    public void useStoryParser(StoryParser storyParser) {
        notAllowed();
    }

    @Override
    public void useStoryReporter(StoryReporter storyReporter) {
        notAllowed();
    }

    @Override
    public void useStepdocReporter(StepdocReporter stepdocReporter) {
        notAllowed();
    }

    @Override
    public void useStepdocGenerator(StepdocGenerator stepdocGenerator) {
        notAllowed();
    }
     
    private void notAllowed() {
        throw new RuntimeException("Configuration elements are unmodifiable");
    }
}