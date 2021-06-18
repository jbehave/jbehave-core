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

public class ItSteps {

    private Stock stock;
    private ExamplesTable table;

    @Given("ho un'azione con simbolo $symbol e una soglia di $threshold")
    public void aStock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
        stock = new Stock(symbol, threshold);
    }

    @When("l'azione è scambiata al prezzo di $price")
    public void stockIsTraded(@Named("price") double price) {
        stock.tradeAt(price);
    }

    @Then("lo status di allerta è $status")
    public void alertStatusIs(@Named("status") String status) {
        assertThat(stock.getStatus().name(), equalTo(status));
    }

    @Given("ho una tabella $table")
    public void aTAble(ExamplesTable table) {
        this.table = table;
    }

    @Then("la tabella ha $rows righe")
    public void hasRows(int rows) {
        assertThat(table.getRowCount(), equalTo(rows));
    }

    @Then("alla riga $row e colonna $column troviamo: $value")
    public void theRowValuesAre(int row, String column, String value) {
        Map<String, String> rowValues = table.getRow(row - 1);
        assertThat(rowValues.get(column), equalTo(value));
    }
}
