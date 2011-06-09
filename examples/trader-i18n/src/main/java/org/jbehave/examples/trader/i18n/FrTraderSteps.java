package org.jbehave.examples.trader.i18n;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.examples.trader.model.Stock;

public class FrTraderSteps {

    private Stock stock;
    private ExamplesTable table;

    @Given("l'on a une action avec symbole $symbol et un seuil de $threshold")
    public void aStock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
        stock = new Stock(symbol, threshold);
    }

    @When("l'action est échangée au prix de $price")
    public void stockIsTraded(@Named("price") double price) {
        stock.tradeAt(price);
    }

    @Then("la position de l'alerte est $status")
    public void alertStatusIs(@Named("status") String status) {
        assertThat(stock.getStatus().name(), equalTo(status));
    }

    @Given("l'on a une table $table")
    public void aTAble(ExamplesTable table) {
        this.table = table;
    }

    @Then("la table a $rows rangs")
    public void hasRows(int rows){
        assertThat(table.getRowCount(), equalTo(rows));
    }

    @Then("au rang $row et en colonne $column on trouve: $value")
    public void theRowValuesAre(int row, String column, String value){
        Map<String,String> rowValues = table.getRow(row-1);      
        assertThat(rowValues.get(column), equalTo(value));
    }
}
