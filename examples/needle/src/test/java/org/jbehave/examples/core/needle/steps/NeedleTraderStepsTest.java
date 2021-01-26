package org.jbehave.examples.core.needle.steps;

import org.hamcrest.Matchers;
import org.jbehave.examples.core.service.TradingService;
import org.junit.Rule;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test Constructor injection for steps.
 * @author Jan Galinski, Holisticon AG
 */
public class NeedleTraderStepsTest {

    // PONR - Plain old needle rule
    @Rule
    public final NeedleRule needle = new NeedleRule();

    // should be created via constructor injection.
    @ObjectUnderTest
    private NeedleTraderSteps needleTraderSteps;

    @Inject
    private TradingService tradingServiceMock;

    @org.junit.Test
    public void shouldCreateNewInstanceViaConstructorInjectionWithMockedService() {
        assertThat(needleTraderSteps, Matchers.is(notNullValue()));
        assertThat(needleTraderSteps.getService(), is(tradingServiceMock));
    }

}
