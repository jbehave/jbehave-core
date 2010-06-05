package org.jbehave.core.configuration;

import org.jbehave.core.model.Keywords;
import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepdocGenerator;

/**
 * PropertyBasedStoryConfiguration is backed by MostUsefulStoryConfiguration as default, but has different
 * behaviour if certain system properties are non-null:
 * <ul>
 *   <li>PropertyBasedStoryConfiguration.FAIL_ON_PENDING: uses  PendingErrorStrategy.FAILING as PendingErrorStrategy</li>
 *   <li>PropertyBasedStoryConfiguration.OUTPUT_ALL:  uses PrintStreamStoryReporter as StoryReporter</li>
 * </ul>
 */
public class PropertyBasedStoryConfiguration extends StoryConfiguration {

    public static final String FAIL_ON_PENDING = "org.jbehave.failonpending";
    public static final String OUTPUT_ALL = "org.jbehave.outputall";
    private final StoryConfiguration defaultConfiguration;
    
    public PropertyBasedStoryConfiguration() {
        this(new MostUsefulStoryConfiguration());
    }

    public PropertyBasedStoryConfiguration(StoryConfiguration defaultConfiguration) {
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
            return new PrintStreamOutput();
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
    public PendingErrorStrategy pendingErrorStrategy() {
        if (System.getProperty(FAIL_ON_PENDING) == null) {
            return defaultConfiguration.pendingErrorStrategy();
        }
        return PendingErrorStrategy.FAILING;
    }

    /**
     * Returns the default StepCollector.
     */
    public StepCollector stepCollector() {
        return defaultConfiguration.stepCollector();
    }

    /**
     * Returns the default ErrorStrategy for handling
     * errors.
     */
    public ErrorStrategy errorStrategy() {
        return defaultConfiguration.errorStrategy();
    }

    /**
     * Returns the default keywords.
     */
    public Keywords keywords() {
        return defaultConfiguration.keywords();
    }

	public StepdocGenerator stepdocGenerator() {
		return defaultConfiguration.stepdocGenerator();
	}

	public StepdocReporter stepdocReporter() {
		return defaultConfiguration.stepdocReporter();
	}

}
