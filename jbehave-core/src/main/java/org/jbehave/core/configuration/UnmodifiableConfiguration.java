package org.jbehave.core.configuration;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.CompositeParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.paranamer.Paranamer;

/**
 * Decorator of Configuration that disables modification of configuration
 * elements.
 */
public class UnmodifiableConfiguration extends Configuration {

    private final Configuration delegate;

    public UnmodifiableConfiguration(Configuration delegate) {
        this.delegate = delegate;
    }

    /**
     * @deprecated Use {@link StoryReporterBuilder}
     */
    @Deprecated
    @Override
    public StoryReporter defaultStoryReporter() {
        return delegate.defaultStoryReporter();
    }

    @Override
    public StoryParser storyParser() {
        return delegate.storyParser();
    }

    @Override
    public CompositeParser compositeParser() {
        return delegate.compositeParser();
    }

    public PendingStepStrategy pendingStepStrategy() {
        return delegate.pendingStepStrategy();
    }

    @Override
    public StepCollector stepCollector() {
        return delegate.stepCollector();
    }

    @Override
    public FailureStrategy failureStrategy() {
        return delegate.failureStrategy();
    }

    @Override
    public Keywords keywords() {
        return delegate.keywords();
    }

    @Override
    public ParameterConverters parameterConverters() {
        return delegate.parameterConverters();
    }

    @Override
    public ParameterControls parameterControls(){
        return delegate.parameterControls();
    }
    
    @Override
    public Paranamer paranamer() {
        return delegate.paranamer();
    }

    @Override
    public ViewGenerator viewGenerator() {
        return delegate.viewGenerator();
    }

    @Override
    public ExamplesTableFactory examplesTableFactory() {
        return delegate.examplesTableFactory();
    }

    @Override
    public StepMonitor stepMonitor() {
        return delegate.stepMonitor();
    }

    @Override
    public StepPatternParser stepPatternParser() {
        return delegate.stepPatternParser();
    }

    @Override
    public boolean dryRun() {
        return delegate.dryRun();
    }

    @Override
    public StoryControls storyControls() {
        return delegate.storyControls();
    }

    @Override
    public StoryLoader storyLoader() {
        return delegate.storyLoader();
    }

    @Override
    public StoryPathResolver storyPathResolver() {
        return delegate.storyPathResolver();
    }

    @Override
    public StoryReporter storyReporter(String storyPath) {
        return delegate.storyReporter(storyPath);
    }

    @Override
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
    public Configuration doDryRun(Boolean dryRun) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryControls(StoryControls storyControls) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryParser(StoryParser storyParser) {
        throw notAllowed();
    }

    @Override
    public Configuration useCompositeParser(CompositeParser compositeParser) {
        throw notAllowed();
    }

    @Override
    public Configuration useDefaultStoryReporter(StoryReporter storyReporter) {
        throw notAllowed();
    }

    @Override
    public Configuration useParameterConverters(ParameterConverters parameterConverters) {
        throw notAllowed();
    }
    
    @Override
    public Configuration useParameterControls(ParameterControls parameterControls) {
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
    public Configuration useStepPatternParser(StepPatternParser stepPatternParser) {
        throw notAllowed();
    }

    @Override
    public Configuration useViewGenerator(ViewGenerator viewGenerator) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryLoader(StoryLoader storyLoader) {
        throw notAllowed();
    }

    @Override
    public Configuration useExamplesTableFactory(ExamplesTableFactory examplesTableFactory) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryPathResolver(StoryPathResolver storyPathResolver) {
        throw notAllowed();
    }

    @Override
    public Configuration useStoryReporterBuilder(StoryReporterBuilder storyReporterBuilder) {
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
        public ModificationNotAllowed(){
            super("Configuration elements are unmodifiable");
        }
    }
}
