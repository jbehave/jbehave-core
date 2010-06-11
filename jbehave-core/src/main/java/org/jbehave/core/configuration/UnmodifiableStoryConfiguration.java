package org.jbehave.core.configuration;

import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.paranamer.Paranamer;

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

    public PendingStepStrategy pendingStepStrategy() {
        return delegate.pendingStepStrategy();
    }

    public StepCollector stepCollector() {
        return delegate.stepCollector();
    }

    public FailureStrategy failureStrategy() {
        return delegate.failureStrategy();
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
    public StoryConfiguration usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
        throw notAllowed();
    }

    @Override
    public StoryConfiguration useErrorStrategy(FailureStrategy failureStrategy) {
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
	public void doDryRun(boolean dryRun) {
        throw notAllowed();
	}

	@Override
	public StoryConfiguration useEmbedderConfiguration(
			EmbedderConfiguration embedderConfiguration) {
        throw notAllowed();
	}

	@Override
	public StoryConfiguration useParameterConverters(
			ParameterConverters parameterConverters) {
        throw notAllowed();
	}

	@Override
	public StoryConfiguration useParanamer(Paranamer paranamer) {
        throw notAllowed();
	}

	@Override
	public StoryConfiguration useStepMonitor(StepMonitor stepMonitor) {
        throw notAllowed();
	}

	@Override
	public StoryConfiguration useStepPatternParser(
			StepPatternParser stepPatternParser) {
        throw notAllowed();
	}

	private RuntimeException notAllowed() {
        return new RuntimeException("Configuration elements are unmodifiable");
    }
}