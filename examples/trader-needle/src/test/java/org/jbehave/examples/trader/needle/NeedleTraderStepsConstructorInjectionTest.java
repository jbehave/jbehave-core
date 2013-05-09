package org.jbehave.examples.trader.needle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.needle.provider.TraderServiceInjectionProvider;
import org.jbehave.examples.trader.service.TradingService;
import org.junit.Rule;
import org.junit.Test;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Assure constructor injection with {@link TradingService} works.
 * 
 * @author Jan Galinski, Holisticon AG
 */
public class NeedleTraderStepsConstructorInjectionTest {

  private static final String BAR = "bar";
  private static final String FOO = "foo";

  @Rule
  public final NeedleRule needleRule = new NeedleRule(new TraderServiceInjectionProvider());

  @ObjectUnderTest
  private NeedleTraderSteps needleTraderSteps;

  @Test
  public void shouldCreateNewInstanceByConstructorInjection() {
    assertNotNull(needleTraderSteps.getService());
    final Trader newTrader = needleTraderSteps.getService().newTrader(FOO, BAR);
    assertNotNull(newTrader);
    assertThat(newTrader.getName(), is(FOO));
    assertThat(newTrader.getRank(), is(BAR));
  }

}
