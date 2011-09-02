package org.jbehave.examples.trader.i18n.steps;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.examples.trader.model.Stock;

public class PtTraderSteps {

    private Stock stock;
    private ExamplesTable table;

    @Given("haja uma ação com símbolo $symbol e um limite de $threshold")
    public void aStock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
        stock = new Stock(symbol, threshold);
    }

    @When("a ação for negociada ao preço de $price")
    public void stockIsTraded(@Named("price") double price) {
        stock.tradeAt(price);
    }

    @Then("a situação de alerta deve ser $status")
    public void alertStatusIs(@Named("status") String status) {
        assertThat(stock.getStatus().name(), equalTo(status));
    }

    @Given("eu tenha uma tabela $table")
    public void aTable(ExamplesTable table) {
        this.table = table;
    }

    @Then("a tabela deve possuir $rows linhas")
    public void hasRows(int rows){
        assertThat(table.getRowCount(), equalTo(rows));
    }

    @Then("na linha $row e coluna $column temos: $value")
    public void theRowValuesAre(int row, String column, String value){
        Map<String,String> rowValues = table.getRow(row-1);      
        assertThat(rowValues.get(column), equalTo(value));
    }
}
