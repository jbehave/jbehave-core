package org.jbehave.core.steps;

import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parser.StepPatternBuilder;

/**
 * Decorator of StepsConfiguration that disables modification of configuration elements.
 */
public class UnmodifiableStepsConfiguration extends StepsConfiguration {

    private final StepsConfiguration delegate;

    public UnmodifiableStepsConfiguration() {
        this(new MostUsefulStepsConfiguration());
    }

    public UnmodifiableStepsConfiguration(StepsConfiguration delegate) {
        this.delegate = delegate;
    }

    @Override
    public StepPatternBuilder patternBuilder() {
        return delegate.patternBuilder();
    }

    @Override
    public void usePatternBuilder(StepPatternBuilder patternBuilder) {
        notAllowed();
    }

    @Override
    public StepMonitor monitor() {
        return delegate.monitor();
    }

    @Override
    public void useMonitor(StepMonitor monitor) {
        notAllowed();
    }

    @Override
    public Paranamer paranamer() {
        return delegate.paranamer();
    }

    @Override
    public void useParanamer(Paranamer paranamer) {
        notAllowed();
    }

    @Override
    public ParameterConverters parameterConverters() {
        return delegate.parameterConverters();
    }

    @Override
    public void useParameterConverters(ParameterConverters parameterConverters) {
        notAllowed();
    }

    @Override
    public Keywords keywords() {
        return delegate.keywords();
    }

    @Override
    public void useKeywords(Keywords keywords) {
        notAllowed();
    }

    private void notAllowed() {
        throw new RuntimeException("Configuration elements are unmodifiable");
    }
}