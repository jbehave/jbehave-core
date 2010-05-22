package org.jbehave.core.steps;

import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.model.Keywords;
import org.jbehave.core.parsers.StepPatternParser;

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
    public StepPatternParser patternParser() {
        return delegate.patternParser();
    }

    @Override
    public StepsConfiguration usePatternParser(StepPatternParser patternParser) {
    	return notAllowed();
    }

    @Override
    public StepMonitor monitor() {
        return delegate.monitor();
    }

    @Override
    public StepsConfiguration useMonitor(StepMonitor monitor) {
        return notAllowed();
    }

    @Override
    public Paranamer paranamer() {
        return delegate.paranamer();
    }

    @Override
    public StepsConfiguration useParanamer(Paranamer paranamer) {
    	return notAllowed();        
    }

    @Override
    public ParameterConverters parameterConverters() {
        return delegate.parameterConverters();
    }

    @Override
    public StepsConfiguration useParameterConverters(ParameterConverters parameterConverters) {
    	return notAllowed();
    }

    @Override
    public Keywords keywords() {
        return delegate.keywords();
    }

    @Override
    public StepsConfiguration useKeywords(Keywords keywords) {
    	return notAllowed();
    }

    private StepsConfiguration notAllowed() {
        throw new RuntimeException("Configuration elements are unmodifiable");
    }
}