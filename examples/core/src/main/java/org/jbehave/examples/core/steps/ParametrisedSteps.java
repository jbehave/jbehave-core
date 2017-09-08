package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.model.ExamplesTable;

public class ParametrisedSteps {

    @Given("a parametrised table: $table")
    public void givenAParametrisedTable(ExamplesTable table){
        String value = table.getRowAsParameters(0, true).valueAs("value", String.class);
        System.out.println(">>>> Replaced row value: "+ value);
    }

    @Given("a value $value")
    public void givenAValue(String value){
    	if ( value.equals("bad") ){
    		throw new RuntimeException("Bad value");
    	}
    }
}
