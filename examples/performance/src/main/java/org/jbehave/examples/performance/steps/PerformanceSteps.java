package org.jbehave.examples.performance.steps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;


public class PerformanceSteps {

    @Given("a step with a long tabular argument: $table")
    public void givenALongTable(ExamplesTable table) {
        
    }
    
    @When("a scenario is generated to $path with a tabular argument of $tabularLines lines and an examples table of"
            + " $examplesLines lines")
    public void aScenarioWithVeryLongTables(String path, int tabularLines, int examplesLines) {
        StringBuilder builder = new StringBuilder();        
        builder.append("Scenario: A scenario with long tables\n");
        builder.append("Given a step with a long tabular argument:\n")  
               .append(aTableWith(tabularLines));        
        builder.append("Examples:\n")       
               .append(aTableWith(examplesLines));
        try {
            FileWriter writer = new FileWriter(new File(path));
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String aTableWith(int numberOfLines) {
        StringBuilder builder = new StringBuilder();        
        builder.append("|h0|h1|h2|h3|h4|h5|h6|h7|h8|h9|\n");
        for (int i = 0; i < numberOfLines; i++) {
            builder.append("|c")
                    .append(i)
                    .append("0|c")
                    .append(i)
                    .append("1|c")
                    .append(i)
                    .append("2|c")
                    .append(i)
                    .append("3|c")
                    .append(i)
                    .append("4|c")
                    .append(i)
                    .append("5|c")
                    .append(i)
                    .append("6|c")
                    .append(i)
                    .append("7|c")
                    .append(i)
                    .append("8|c")
                    .append(i)
                    .append("9|\n");
        }
        return builder.toString();
    }

}
