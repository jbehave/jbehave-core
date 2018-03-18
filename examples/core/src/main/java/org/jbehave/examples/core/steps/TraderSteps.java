package org.jbehave.examples.core.steps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.Parameters;
import org.jbehave.examples.core.CoreStory;
import org.jbehave.examples.core.model.Stock;
import org.jbehave.examples.core.model.Stock.AlertStatus;
import org.jbehave.examples.core.model.Trader;
import org.jbehave.examples.core.persistence.TraderPersister;
import org.jbehave.examples.core.service.TradingService;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

/**
 * POJO holding the candidate steps for the trader example.  
 * The {@link CandidateSteps} instance wrapping this are created via the {@link InstanceStepsFactory}
 * in the {@link CoreStory}.
 */
public class TraderSteps {

	private TradingService service;
	private ThreadLocal<Stock> stock = new ThreadLocal<Stock>();
	private Trader trader;
	private List<Trader> traders = new ArrayList<Trader>();
	private List<Trader> searchedTraders;
	private Date date;
	private ExamplesTable ranksTable;
	private String stockExchange;
	private String assetClass;
    private TradeType tradeType;

	public TraderSteps() {
		this(new TradingService());
	}

	public TraderSteps(TradingService service) {
		this.service = service;
	}

	public TradingService getService() {
		return this.service;
	}

	@Given("a date of $date")
	public void aDate(Date date) {
		this.date = date;
	}

	@When("$days days pass")
	public void daysPass(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, days);
		date = calendar.getTime();
	}

	@Then("the date is $date")
	public void theDate(Date date) {
		assertThat(date, equalTo(this.date));
	}

	@Given("a trader of {name|id} $trader")
	public void aTrader(Trader trader) {
		this.trader = trader;
	}

	@Given("the trader ranks: $ranksTable")
	@Alias("the traders: $ranksTable")
	public void theTraderRanks(ExamplesTable ranksTable) {
		this.ranksTable = ranksTable;
		traders.clear();
		traders.addAll(toTraders(ranksTable));
	}

	@When("traders are subset to \"$regex\" by name")
	@Alias("traders are filtered by \"$regex\"")
	public void subsetTradersByName(String regex) {
		searchedTraders = new ArrayList<Trader>();
		for (Trader trader : traders) {
			if (trader.getName().matches(regex)) {
				searchedTraders.add(trader);
			}
		}
	}

	@Then("the current trader activity is: $activityTable")
	public void theTradersActivityIs(ExamplesTable activityTable) {
		for (int i = 0; i < activityTable.getRowCount(); i++) {
			Parameters row = activityTable.withDefaults(this.ranksTable.getRowAsParameters(i)).getRowAsParameters(i);
			System.out.println(row.valueAs("name", Trader.class) + " ("
					+ row.valueAs("rank", String.class, "N/A") + ") has done " + row.valueAs("trades", Integer.class)
					+ " trades");
		}
	}

	@Then("the traders returned are: $tradersTable")
	public void theTradersReturnedAre(ExamplesTable tradersTable) {
		OutcomesTable outcomes = new OutcomesTable();
		outcomes.addOutcome("traders", searchedTraders.toString(), equalTo(toTraders(tradersTable).toString()));
		outcomes.addOutcome("a success", "Any Value", equalTo("Any Value"));
		outcomes.verify();
	}

	private List<Trader> toTraders(ExamplesTable table) {
		List<Trader> traders = new ArrayList<Trader>();
		List<Map<String, String>> rows = table.getRows();
		for (Map<String, String> row : rows) {
			String name = row.get("name");
			String rank = row.get("rank");
			traders.add(getService().newTrader(name, rank));
		}
		Collections.sort(traders);
		return traders;
	}

	@Given("the stock exchange $stockExchange")
	@Alias("the stock exchange <stockExchange>")
	public void theStockExchange(@Named("stockExchange") String stockExchange) {
		this.stockExchange = stockExchange;
	}

	@Given("the asset class $assetClass")
	@Alias("the asset class <assetClass>")
	public void theAssetClass(@Named("assetClass") String assetClass) {
		this.assetClass = assetClass;
	}

	@Given("a stock of symbol $symbol and a threshold of $threshold")
	@Alias("a stock of <symbol> and a <threshold>")
	// alias used with examples table
	public void aStock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
		stock.set(getService().newStock(symbol, threshold));
	}

	@When("the stock is traded at price $price")
	@Aliases(values = { "the stock is sold at price $price", "the stock is exchanged at price $price",
			"the stock is traded with <price>" })
	// multiple aliases, one used with examples table
	public void theStockIsTraded(@Named("price") double price) {
		stock.get().tradeAt(price);
	}

	@Given("the alert status is $status")
	// shows that matching pattern need only be unique for step type
	public void theAlertStatusIsReset(@Named("status") String status) {
		if (AlertStatus.OFF.name().startsWith(status) && stock.get() != null) {
			stock.get().resetAlert();
		}
	}

	@Then("the alert status is $status")
	@Alias("the trader is alerted with <status>")
	// alias used with examples table
	public void theAlertStatusIs(@Named("status") String status) {
		assertThat(stock.get().getStatus().name(), equalTo(status));
	}

	@Then(value = "the alert status is currently $status", priority = 1)
	// prioritise over potential match with previous method
	public void theAlertStatusIsCurrently(@Named("status") String status) {
		assertThat(stock.get().getStatus().name(), equalTo(status));
	}

	@When("the trader sells all stocks")
	public void theTraderSellsAllStocks() {
		trader.sellAllStocks();
	}

	@Then("the trader is left with no stocks")
	public void theTraderIsLeftWithNoStocks() {
		assertThat(trader.getStocks().size(), equalTo(0));
	}

    @Given("a trade type $tradeType")
    public void givenATradeType(TradeType type) {
        this.tradeType = type;
    }

    @Then("the current trade type is $type")
    public void thenTheCurrentTradeTypeIs(String type) {
        assertThat(this.tradeType.name(), equalTo(type));
    }

    @Then("the list of trade types is $types")
    public void thenTheListTradeTypesIs(List<TradeType> types) {
        List<String> values = new ArrayList<String>();
        for (TradeType type : TradeType.values()) {
            values.add(type.name());
        }
        assertThat(types.toString(), equalTo(values.toString()));
    }

    enum TradeType {
        BUY, SELL;
    }

	// Method used as dynamical parameter converter
	@AsParameterConverter
	public Trader retrieveTrader(String name) {
		for (Trader trader : traders) {
			if (trader.getName().equals(name)) {
				return trader;
			}
		}
		return mockTradePersister().retrieveTrader(name);
	}

	static TraderPersister mockTradePersister() {
		return new TraderPersister(new Trader("Mauro", asList(new Stock("STK1", 10.d))));
	}
	
}

