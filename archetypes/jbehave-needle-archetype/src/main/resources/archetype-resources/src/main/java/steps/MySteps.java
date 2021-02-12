#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.steps;

import javax.inject.Inject;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import ${package}.steps.SpecialService;
import ${package}.steps.UsualService;

public class MySteps {

    @Inject
    private UsualService stubbedService;

    @Inject
    private SpecialService service;

    @Then("I shall be happy")
    public void makeHappy() {
        service.makeHappy();
    }

    @Given("I am a pending step")
    public void givenIAmAPendingStep() {
        stubbedService.aStep();
    }

    @Given("I am still pending step")
    public void givenIAmStillPendingStep() {
        stubbedService.isStillStep();
    }

    @When("a good soul will implement me")
    public void whenAGoodSoulWillImplementMe() {
        stubbedService.goodSoul();
    }

}
