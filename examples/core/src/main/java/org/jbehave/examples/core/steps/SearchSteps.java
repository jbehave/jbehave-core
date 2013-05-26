package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class SearchSteps {
    @Given("that I am on Google's Homepage")
    public void onGoogle() {
        System.out.println(" ... on Google!");
    }

    @When("I enter the search term <ridiculousSearchTerm> and proceed")
    public void enterSearchTermAndProceed(@Named("ridiculousSearchTerm") String ridiculousSearchTerm) {
        System.out.println(" ... entering " + ridiculousSearchTerm + " into box and clicking continue!");
    }

    @Then("I should see ridiculous things")
    public void seeResults() {
        System.out.println(" ... ahhh, so much pink!!!");
    }

    @AfterStory
    public void killBrowser() {
        System.out.println(" ... Browser has been put to rest \n\n");
    }

}