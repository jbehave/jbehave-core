package org.jbehave.examples.trader.i18n.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Locale;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.steps.Parameters;
import org.jbehave.examples.core.model.Stock;

public class FrSteps {

    private Stock stock;
    private ExamplesTable table;

    @Given("l'on a une action avec symbole $symbol et un seuil de $threshold")
    public void stock(@Named("symbol") String symbol, @Named("threshold") double threshold) {
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
    public void table(ExamplesTable table) {
        this.table = table;
    }

    @Then("la table a $rows rangs")
    public void hasRows(int rows) {
        assertThat(table.getRowCount(), equalTo(rows));
    }

    @Then("au rang $row et en colonne $column on trouve: $value")
    public void theRowValuesAre(int row, String column, String value) {
        Map<String, String> rowValues = table.getRow(row - 1);
        assertThat(rowValues.get(column), equalTo(value));
    }

    @Then("les valeurs multipliées par $multiplier sont: $table")
    public void theResultsMultipliedByAre(int multiplier, ExamplesTable results) {
        OutcomesTable outcomes = new OutcomesTable(new LocalizedKeywords(new Locale("fr")));
        for (int row = 0; row < results.getRowCount(); row++) {
            Parameters expected = results.getRowAsParameters(row);
            Parameters original = table.getRowAsParameters(row);
            int one = original.valueAs("un", Integer.class);
            int two = original.valueAs("deux", Integer.class);
            outcomes.addOutcome("un", one * multiplier, equalTo(expected.valueAs("un", Integer.class)));
            outcomes.addOutcome("deux", two * multiplier, equalTo(expected.valueAs("deux", Integer.class)));
        }
        outcomes.verify();
    }

}
