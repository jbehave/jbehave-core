package org.jbehave.examples.trader.i18n;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;

import java.util.Locale;

import org.jbehave.examples.trader.model.Stock;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Named;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.definition.KeyWords;
import org.jbehave.scenario.i18n.I18nKeyWords;
import org.jbehave.scenario.i18n.StringEncoder;
import org.jbehave.scenario.steps.Steps;
import org.jbehave.scenario.steps.StepsConfiguration;

public class ItTraderSteps extends Steps {

    private Stock stock;

    public ItTraderSteps(ClassLoader classLoader) {
    	// Use Italian for keywords
        super(new StepsConfiguration(keywordsFor(new Locale("it"), classLoader)));
    }

    @Given("ho un'azione con simbolo $symbol e una soglia di $threshold")
    public void aStock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
        stock = new Stock(symbol, threshold);
    }

    @When("l'azione e' scambiata al prezzo di $price")
    public void stockIsTraded(@Named("price") double price) {
        stock.tradeAt(price);
    }

    @Then("lo status di allerta e' $status")
    public void alertStatusIs(@Named("status") String status) {
        ensureThat(stock.getStatus().name(), equalTo(status));
    }

	protected static KeyWords keywordsFor(Locale locale, ClassLoader classLoader) {
		return new I18nKeyWords(locale, new StringEncoder(), "org/jbehave/examples/trader/i18n/keywords", classLoader);
	}

}
