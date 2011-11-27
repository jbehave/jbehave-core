package org.jbehave.examples.performance.steps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;


public class PerformanceSteps {

    @Given("a step with a long tabular argument: $table")
    public void givenALongTable(ExamplesTable table){
        
    }
    
    @When("a scenario is generated to $path with a tabular argument of $tabularLines lines and an examples table of $examplesLines lines")
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
            builder.append("|c"+i+"0|c"+i+"1|c"+i+"2|c"+i+"3|c"+i+"4|c"+i+"5|c"+i+"6|c"+i+"7|c"+i+"8|c"+i+"9|\n");
        }
        return builder.toString();
    }

}
