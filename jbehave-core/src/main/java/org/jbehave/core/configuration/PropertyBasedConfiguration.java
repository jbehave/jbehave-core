package org.jbehave.core.configuration;

import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector;

/**
 * PropertyBasedConfiguration is backed by MostUsefulConfiguration as default, but has different
 * behaviour if certain system properties are non-null:
 * <ul>
 *   <li>PropertyBasedConfiguration.FAIL_ON_PENDING: uses {@link FailingUponPendingStep}</li>
 *   <li>PropertyBasedConfiguration.OUTPUT_ALL:  uses {@link ConsoleOutput}</li>
 * </ul>
 */
public class PropertyBasedConfiguration extends Configuration {

    public static final String FAIL_ON_PENDING = "org.jbehave.failonpending";
    public static final String OUTPUT_ALL = "org.jbehave.outputall";
    private final Configuration defaultConfiguration;
    
    public PropertyBasedConfiguration() {
        this(new MostUsefulConfiguration());
    }

    public PropertyBasedConfiguration(Configuration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    /**
     * If the system property org.jbehave.outputall
     * is set to TRUE, uses a PrintStreamStoryReporter;
     * otherwise uses the default StoryReporter.
     * 
     * Setting org.jbehave.outputall will allow you
     * to see the steps for all stories, regardless
     * of whether the stories fail.
     */
    public StoryReporter storyReporter() {
        if (System.getProperty(OUTPUT_ALL) == null) {
            return defaultConfiguration.storyReporter();
        } else {
            return new ConsoleOutput();
        }
    }

    /**
     * Returns the default StoryParser.
     */
    public StoryParser storyParser() {
        return defaultConfiguration.storyParser();
    }

    /**
     * If the system property org.jbehave.failonpending
     * is non-null, returns PendingStepStrategy.FAILING,
     * otherwise returns the defaults.
     * 
     * <p>Setting org.jbehave.failonpending will cause
     * pending steps to throw an error,
     * so you can see if any steps don't match or are
     * still to be implemented.
     */
    public PendingStepStrategy pendingStepStrategy() {
        if (System.getProperty(FAIL_ON_PENDING) == null) {
            return defaultConfiguration.pendingStepStrategy();
        }
        return new FailingUponPendingStep();
    }

    /**
     * Returns the default StepCollector.
     */
    public StepCollector stepCollector() {
        return defaultConfiguration.stepCollector();
    }

    /**
     * Returns the default FailureStrategy for handling
     * errors.
     */
    public FailureStrategy failureStrategy() {
        return defaultConfiguration.failureStrategy();
    }

    /**
     * Returns the default keywords.
     */
    public Keywords keywords() {
        return defaultConfiguration.keywords();
    }

}
