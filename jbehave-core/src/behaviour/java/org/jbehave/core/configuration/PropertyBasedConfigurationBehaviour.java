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

public class PropertyBasedConfigurationBehaviour {

    private String originalFailOnPending;
    private String originalOutputAll;

    @Before
    public void captureExistingEnvironment() {
        originalFailOnPending = System.getProperty(PropertyBasedConfiguration.FAIL_ON_PENDING);
        originalOutputAll = System.getProperty(PropertyBasedConfiguration.OUTPUT_ALL);
    }
    
    @After
    public void resetEnvironment() {
        if (originalFailOnPending != null) {
            System.setProperty(PropertyBasedConfiguration.FAIL_ON_PENDING, originalFailOnPending);
        } else {
            System.clearProperty(PropertyBasedConfiguration.FAIL_ON_PENDING);
        }
        if (originalOutputAll != null) {
            System.setProperty(PropertyBasedConfiguration.OUTPUT_ALL, originalOutputAll);
        } else {
            System.clearProperty(PropertyBasedConfiguration.OUTPUT_ALL);
        }
    }
    
    @Test
    public void shouldUsePassingPendingStepStrategyByDefault() {
        System.clearProperty(PropertyBasedConfiguration.FAIL_ON_PENDING);
        assertThat(new PropertyBasedConfiguration().pendingStepStrategy(), is(PassingUponPendingStep.class));
    }
    
    @Test
    public void shouldUseFailingPendingStepStrategyWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedConfiguration.FAIL_ON_PENDING, "true");
        assertThat(new PropertyBasedConfiguration().pendingStepStrategy(), is(FailingUponPendingStep.class));
    }
    
    @Test
    public void shouldOuputToConsoleByDefault() {
        System.clearProperty(PropertyBasedConfiguration.OUTPUT_ALL);
        assertThat(new PropertyBasedConfiguration().storyReporter(), is(ConsoleOutput.class));
    }
    
    @Test
    public void shouldOutputAllWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedConfiguration.OUTPUT_ALL, "true");
        assertThat(new PropertyBasedConfiguration().storyReporter(), is(ConsoleOutput.class));
    }
    
    @Test
    public void shouldRethrowFailures() {
        assertThat(new PropertyBasedConfiguration().failureStrategy(), is(RethrowingFailure.class));
    }
    
    @Test
    public void shouldProvideGivenWhenThenKeywordsByDefault() {
        assertThat(new PropertyBasedConfiguration().keywords(), is(LocalizedKeywords.class));
    }
}
