package org.jbehave.examples.trader.i18n.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.examples.core.model.Stock;

public class DeSteps {

    private Stock stock;
    private ExamplesTable table;

    // Actually it would be spelled "symbol" in German, but
    // we want to verify that umlauts work for parameter names.
    @Given("ich habe eine Aktion mit dem Symbol $sümbol und eine Schwelle von $threshold")
    public void aStock(@Named("sümbol") String symbol, @Named("threshold") double threshold) {
        stock = new Stock(symbol, threshold);
    }

    // Actually it would be spelled "gehandelt" in German, but
    // we want to verify that umlauts work for step names.
    @When("die Aktie zum Preis $price gehändelt wird")
    public void stockIsTraded(@Named("price") double price) {
        stock.tradeAt(price);
    }

    @Then("ist der Status der Meldung $status")
    public void alertStatusIs(@Named("status") String status) {
        assertThat(stock.getStatus().name(), equalTo(status));
    }

    @Given("ich habe die Tabelle $table")
    public void aTable(ExamplesTable table) {
        this.table = table;
    }

    @Then("hat die Tabelle $rows Zeilen")
    public void hasRows(int rows) {
        assertThat(table.getRowCount(), equalTo(rows));
    }

    @Then("in Zeile $row und Spalte $column ist: $value")
    public void theRowValuesAre(int row, String column, String value) {
        Map<String,String> rowValues = table.getRow(row-1);      
        assertThat(rowValues.get(column), equalTo(value));
    }

}
