package org.jbehave.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.reporters.SilentSuccessFilter;
import org.jbehave.core.reporters.PrintStreamOutput;
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
        assertThat(new PropertyBasedStoryConfiguration().pendingErrorStrategy(), is(PendingErrorStrategy.PASSING));
    }
    
    @Test
    public void shouldUseFailingPendingStepStrategyWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedStoryConfiguration.FAIL_ON_PENDING, "true");
        assertThat(new PropertyBasedStoryConfiguration().pendingErrorStrategy(), is(PendingErrorStrategy.FAILING));
    }
    
    @Test
    public void shouldSwallowOutputFromPassingScenariossByDefault() {
        System.clearProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL);
        assertThat(new PropertyBasedStoryConfiguration().storyReporter(), is(SilentSuccessFilter.class));
    }
    
    @Test
    public void shouldOutputAllWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedStoryConfiguration.OUTPUT_ALL, "true");
        assertThat(new PropertyBasedStoryConfiguration().storyReporter(), is(PrintStreamOutput.class));
    }
    
    @Test
    public void shouldRethrowErrrors() {
        assertThat(new PropertyBasedStoryConfiguration().errorStrategy(), equalTo(ErrorStrategy.RETHROW));
    }
    
    @Test
    public void shouldProvideGivenWhenThenKeywordsByDefault() {
        assertThat(new PropertyBasedStoryConfiguration().keywords(), is(LocalizedKeywords.class));
    }
}
