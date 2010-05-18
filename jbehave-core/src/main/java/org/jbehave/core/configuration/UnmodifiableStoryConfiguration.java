package org.jbehave.core.configuration;

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
    public StoryConfiguration useKeywords(Keywords keywords) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useStepCreator(StepCreator stepCreator) {
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

    @Override
    public StoryConfiguration useStepdocReporter(StepdocReporter stepdocReporter) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useStepdocGenerator(StepdocGenerator stepdocGenerator) {
        throw notAllowed();
    }
     
    private RuntimeException notAllowed() {
        return new RuntimeException("Configuration elements are unmodifiable");
    }
}