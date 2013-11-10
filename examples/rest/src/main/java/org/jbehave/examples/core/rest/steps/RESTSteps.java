package org.jbehave.examples.core.rest.steps;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;

public class RESTSteps {

    private String text;

    @When("a wiki page is retrieved as $type from Redmine at $uri")
    public void resourcesAreRetrievedFromRedmine(String type, String uri){
        LoadFromRedmine loadFromRedmine = new LoadFromRedmine(Type.valueOf(type));
        text = loadFromRedmine.loadResourceAsText(uri);
    }

    @Then("the page contains the themes")
    public void pageContainsThemes(){
        assertThat(text, containsString("Themes"));
    }

}
