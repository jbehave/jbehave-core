package org.jbehave.core.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.reporters.ConsoleOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PropertyBasedStoryConfigurationBehaviour {

    private String originalFailOnPending;
    private String originalOutputAll;

    @Before
    public void captureExistingEnvironment() {
        originalFailOnPending = System.getProperty(PropertyBasedStoryConfiguration.FAIL_ON_PENDING);
        originalOutputAll = System.getProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL);
    }
    
    @After
    public void resetEnvironment() {
        if (originalFailOnPending != null) {
            System.setProperty(PropertyBasedStoryConfiguration.FAIL_ON_PENDING, originalFailOnPending);
        } else {
            System.clearProperty(PropertyBasedStoryConfiguration.FAIL_ON_PENDING);
        }
        if (originalOutputAll != null) {
            System.setProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL, originalOutputAll);
        } else {
            System.clearProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL);
        }
    }
    
    @Test
    public void shouldUsePassingPendingStepStrategyByDefault() {
        System.clearProperty(PropertyBasedStoryConfiguration.FAIL_ON_PENDING);
        assertThat(new PropertyBasedStoryConfiguration().pendingStepStrategy(), is(PassingUponPendingStep.class));
    }
    
    @Test
    public void shouldUseFailingPendingStepStrategyWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedStoryConfiguration.FAIL_ON_PENDING, "true");
        assertThat(new PropertyBasedStoryConfiguration().pendingStepStrategy(), is(FailingUponPendingStep.class));
    }
    
    @Test
    public void shouldOuputToConsoleByDefault() {
        System.clearProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL);
        assertThat(new PropertyBasedStoryConfiguration().storyReporter(), is(ConsoleOutput.class));
    }
    
    @Test
    public void shouldOutputAllWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL, "true");
        assertThat(new PropertyBasedStoryConfiguration().storyReporter(), is(ConsoleOutput.class));
    }
    
    @Test
    public void shouldRethrowErrrors() {
        assertThat(new PropertyBasedStoryConfiguration().failureStrategy(), is(RethrowingFailure.class));
    }
    
    @Test
    public void shouldProvideGivenWhenThenKeywordsByDefault() {
        assertThat(new PropertyBasedStoryConfiguration().keywords(), is(LocalizedKeywords.class));
    }
}
