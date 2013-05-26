package org.jbehave.examples.trader.i18n.steps;

import org.jbehave.core.annotations.Given;

public class SvSteps {

    @Given("att vi finns")
    public void exist() {
        System.out.println("Exists");
    }

    @Given("att det har startat")
    public void started() {
        System.out.println("Started");
    }
    
}
