package org.jbehave.scenario;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.jbehave.Ensure.ensureThat;

import org.jbehave.scenario.errors.ErrorStrategy;
import org.jbehave.scenario.errors.PendingErrorStrategy;
import org.jbehave.scenario.i18n.I18nKeyWords;
import org.jbehave.scenario.reporters.PassSilentlyDecorator;
import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;
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
        ensureThat(new PropertyBasedConfiguration().forPendingSteps(), is(PendingErrorStrategy.PASSING));
    }
    
    @Test
    public void shouldUseFailingPendingStepStrategyWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedConfiguration.FAIL_ON_PENDING, "true");
        ensureThat(new PropertyBasedConfiguration().forPendingSteps(), is(PendingErrorStrategy.FAILING));
    }
    
    @Test
    public void shouldSwallowOutputFromPassingScenariosByDefault() {
        System.clearProperty(PropertyBasedConfiguration.OUTPUT_ALL);
        ensureThat(new PropertyBasedConfiguration().forReportingScenarios(), is(PassSilentlyDecorator.class));
    }
    
    @Test
    public void shouldOutputAllWhenConfiguredToDoSo() {
        System.setProperty(PropertyBasedConfiguration.OUTPUT_ALL, "true");
        ensureThat(new PropertyBasedConfiguration().forReportingScenarios(), is(PrintStreamScenarioReporter.class));
    }
    
    @Test
    public void shouldRethrowErrrors() {
        ensureThat(new PropertyBasedConfiguration().forHandlingErrors(), equalTo(ErrorStrategy.RETHROW));
    }
    
    @Test
    public void shouldProvideGivenWhenThenKeywordsByDefault() {
        ensureThat(new PropertyBasedConfiguration().keywords(), is(I18nKeyWords.class));
    }
}
