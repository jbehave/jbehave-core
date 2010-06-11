package org.jbehave.core.configuration;

import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.paranamer.Paranamer;

/**
 * Decorator of Configuration that disables modification of configuration elements.
 */
public class UnmodifiableConfiguration extends Configuration {

    private final Configuration delegate;

    public UnmodifiableConfiguration() {
        this(new MostUsefulConfiguration());
    }

    public UnmodifiableConfiguration(Configuration delegate) {
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
    public Configuration useKeywords(Keywords keywords) {
        throw notAllowed();
    }

    @Override
    public Configuration useStepCollector(StepCollector stepCollector) {
        throw notAllowed();
    }

    @Override
    public Configuration usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
        throw notAllowed();
    }

    @Override
    public Configuration useErrorStrategy(FailureStrategy failureStrategy) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryParser(StoryParser storyParser) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryReporter(StoryReporter storyReporter) {
        throw notAllowed();
    }
    
    @Override
	public void doDryRun(boolean dryRun) {
        throw notAllowed();
	}

	@Override
	public Configuration useEmbedderControls(
			EmbedderControls embedderControls) {
        throw notAllowed();
	}

	@Override
	public Configuration useParameterConverters(
			ParameterConverters parameterConverters) {
        throw notAllowed();
	}

	@Override
	public Configuration useParanamer(Paranamer paranamer) {
        throw notAllowed();
	}

	@Override
	public Configuration useStepMonitor(StepMonitor stepMonitor) {
        throw notAllowed();
	}

	@Override
	public Configuration useStepPatternParser(
			StepPatternParser stepPatternParser) {
        throw notAllowed();
	}

	private RuntimeException notAllowed() {
        return new RuntimeException("Configuration elements are unmodifiable");
    }
}