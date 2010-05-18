package org.jbehave.examples.trader;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepsFactory;
import org.jbehave.examples.trader.model.Stock;
import org.jbehave.examples.trader.model.Stock.AlertStatus;
import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.service.TradingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * POJO holding the candidate steps for the trader example.  
 * The {@link CandidateSteps} instance wrapping this are created via the {@link StepsFactory}
 * in the {@link TraderStory}.
 */
public class TraderSteps {

    private TradingService service;    
    private Stock stock;
    private Trader trader;
    private List<Trader> traders = new ArrayList<Trader>();
    private List<Trader> searchedTraders;
        
    public TraderSteps(TradingService service) {
        this.service = service;
    }

    @Given("a trader of name %trader")
    public void aTrader(Trader trader) {
        this.trader = trader;
    }

    @Given("the traders: %tradersTable")
    public void theTraders(ExamplesTable tradersTable) {
        traders.clear();
        traders.addAll(toTraders(tradersTable));
    }

    @When("traders are subset to \"%regex\" by name")
    @Alias("traders are filtered by \"%regex\"")
    public void subsetTradersByName(String regex) {
        searchedTraders = new ArrayList<Trader>();
        for (Trader trader : traders) {
            if ( trader.getName().matches(regex) ){
                searchedTraders.add(trader);
            }
        }
    }
    
    @Then("the traders returned are: %tradersTable")
    public void theTradersReturnedAre(ExamplesTable tradersTable) {
        List<Trader> expected = toTraders(tradersTable);
        assertEquals(expected.toString(), searchedTraders.toString());
    }

    private List<Trader> toTraders(ExamplesTable table) {
        List<Trader> traders = new ArrayList<Trader>();
        List<Map<String, String>> rows = table.getRows();
        for (Map<String, String> row : rows) {
            String name = row.get("name");
            String rank = row.get("rank");
            traders.add(service.newTrader(name, rank));
        }
        Collections.sort(traders);
        return traders;
    }
    
    @Given("a stock of symbol %symbol and a threshold of %threshold")
    @Alias("a stock of <symbol> and a <threshold>") // alias used with examples table
    public void aStock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
        stock = service.newStock(symbol, threshold);
    }

    @When("the stock is traded at price %price")
    @Aliases(values={"the stock is sold at price %price", "the stock is exchanged at price %price",
            "the stock is traded with <price>"}) // multiple aliases, one used with examples table
    public void theStockIsTraded(@Named("price") double price) {
        stock.tradeAt(price);
    }

    @Given("the alert status is %status") // shows that matching pattern need only be unique for step type
    public void theAlertStatusIsReset(@Named("status") String status) {
    	if ( AlertStatus.OFF.name().startsWith(status) && stock != null ){
        	stock.resetAlert();    		
    	}
    }

    @Then("the alert status is %status")
    @Alias("the trader is alerted with <status>") // alias used with examples table
    public void theAlertStatusIs(@Named("status") String status) {
        assertThat(stock.getStatus().name(), equalTo(status));
    }

    @Then(value="the alert status is currently %status", priority=1) // prioritise over potential match with previous method
    public void theAlertStatusIsCurrently(@Named("status") String status) {
        assertThat(stock.getStatus().name(), equalTo(status));
    }    
    
    @When("the trader sells all stocks")
    public void theTraderSellsAllStocks() {
        trader.sellAllStocks();
    }

    @Then ("the trader is left with no stocks")
    public void theTraderIsLeftWithNoStocks() {
        assertThat(trader.getStocks().size(), equalTo(0));
    }

}
