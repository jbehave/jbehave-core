package org.jbehave.core.configuration;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ReportRenderer;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.StepdocGenerator;

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

	public Configuration buildReporters(List<String> storyPaths) {
		return delegate.buildReporters(storyPaths);
	}

	public Configuration buildReporters(String... storyPaths) {
		return delegate.buildReporters(storyPaths);
	}

	public boolean dryRun() {
		return delegate.dryRun();
	}

	public EmbedderControls embedderControls() {
		return delegate.embedderControls();
	}

	public ParameterConverters parameterConverters() {
		return delegate.parameterConverters();
	}

	public Paranamer paranamer() {
		return delegate.paranamer();
	}

	public ReportRenderer reportRenderer() {
		return delegate.reportRenderer();
	}

	public StepdocGenerator stepdocGenerator() {
		return delegate.stepdocGenerator();
	}

	public StepdocReporter stepdocReporter() {
		return delegate.stepdocReporter();
	}

	public StepMonitor stepMonitor() {
		return delegate.stepMonitor();
	}

	public StepPatternParser stepPatternParser() {
		return delegate.stepPatternParser();
	}

	public StoryLoader storyLoader() {
		return delegate.storyLoader();
	}

	public StoryPathResolver storyPathResolver() {
		return delegate.storyPathResolver();
	}

	public StoryReporter storyReporter(String storyPath) {
		return delegate.storyReporter(storyPath);
	}

	public StoryReporterBuilder storyReporterBuilder() {
		return delegate.storyReporterBuilder();
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
    public Configuration useFailureStrategy(FailureStrategy failureStrategy) {
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
	
	@Override
	public void useReportRenderer(ReportRenderer reportRenderer) {
        throw notAllowed();
	}

	@Override
	public void useStepdocGenerator(StepdocGenerator stepdocGenerator) {
        throw notAllowed();
	}

	@Override
	public void useStepdocReporter(StepdocReporter stepdocReporter) {
        throw notAllowed();
	}

	@Override
	public Configuration useStoryLoader(StoryLoader storyLoader) {
        throw notAllowed();
	}

	@Override
	public Configuration useStoryPathResolver(
			StoryPathResolver storyPathResolver) {
        throw notAllowed();
	}

	@Override
	public Configuration useStoryReporter(String storyPath,
			StoryReporter storyReporter) {
        throw notAllowed();
	}

	@Override
	public Configuration useStoryReporterBuilder(
			StoryReporterBuilder storyReporterBuilder) {
        throw notAllowed();
	}

	@Override
	public Configuration useStoryReporters(
			Map<String, StoryReporter> storyReporters) {
        throw notAllowed();
	}

	private RuntimeException notAllowed() {
        return new RuntimeException("Configuration elements are unmodifiable");
    }
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(delegate).toString();
	}
	
}