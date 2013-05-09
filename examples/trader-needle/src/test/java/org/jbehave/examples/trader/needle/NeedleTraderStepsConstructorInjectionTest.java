package org.jbehave.examples.trader.needle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.service.TradingService;
import org.junit.Rule;
import org.junit.Test;

import de.akquinet.jbosscc.needle.annotation.InjectIntoMany;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

public class NeedleTraderStepsConstructorInjectionTest {

  private static final String BAR = "bar";
  private static final String FOO = "foo";

  @Rule
  public final NeedleRule needleRule = new NeedleRule();

  /**
   * Constructor Injection on needleTraderSteps works. But needle creates a mock
   * of {@link TradingService} by default. By using {@link InjectIntoMany}, a
   * concrete instance is bound.
   */
  @InjectIntoMany
  private final TradingService tradingService = new TradingService();

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
