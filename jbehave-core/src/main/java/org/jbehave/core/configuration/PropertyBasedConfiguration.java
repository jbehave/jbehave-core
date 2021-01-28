package org.jbehave.core.configuration;

import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.reporters.SilentSuccessFilter;
import org.jbehave.core.reporters.StoryReporter;

/**
 * PropertyBasedConfiguration is backed by MostUsefulConfiguration as default,
 * but has different behaviour if certain system properties are set:
 * <ul>
 * <li>{@link #FAIL_ON_PENDING}: uses {@link FailingUponPendingStep}</li>
 * <li>{@link #SILENT_SUCCESS}: uses {@link SilentSuccessFilter} decorator</li>
 * </ul>
 */
public class PropertyBasedConfiguration extends MostUsefulConfiguration {

	public static final String FAIL_ON_PENDING = "org.jbehave.core.configuration.failonpending";
	public static final String SILENT_SUCCESS = "org.jbehave.core.configuration.silentsuccess";

	/**
	 * <p>
	 * If the system property {@link #SILENT_SUCCESS} is set, uses a
	 * {@link SilentSuccessFilter} to decorate the default StoryReporter.
	 * </p>
	 * <p>
	 * Setting {@link #SILENT_SUCCESS} will only show the steps for all stories
	 * if the stories fail.
	 * </p>
	 */
    @Override
    public StoryReporter defaultStoryReporter() {
		StoryReporter storyReporter = super.defaultStoryReporter();
		if (System.getProperty(SILENT_SUCCESS) == null) {
			return storyReporter;
		} else {
			return new SilentSuccessFilter(storyReporter);
		}
	}
	
	/**
	 * <p>
	 * If the system property {@link #FAIL_ON_PENDING} is set, returns
	 * {@link FailingUponPendingStep} otherwise returns the default.
	 * </p>
	 * <p>
	 * Setting {@link #FAIL_ON_PENDING} will cause pending steps to fail story
	 * execution, so you can see if any steps don't match or are still to be
	 * implemented.
	 * </p>
	 */
	@Override
    public PendingStepStrategy pendingStepStrategy() {
		if (System.getProperty(FAIL_ON_PENDING) == null) {
			return super.pendingStepStrategy();
		}
		return new FailingUponPendingStep();
	}

}
