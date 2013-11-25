package org.jbehave.examples.core.rest.steps;

import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.redmine.ListFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;

public class RESTSteps {

    private String text;
    private List<String> list;

    @When("list of pages is retrieved from Redmine at $uri")
    public void indexIsRetrievedFromRedmine(String uri){
        ListFromRedmine loadFromRedmine = new ListFromRedmine();
        list = loadFromRedmine.listResources(uri);        
    }
    
    @Then("the index contains $number stories")
    public void indexContainsStories(int number){
        assertThat(list.size(), Matchers.equalTo(number));
    }

    @When("a wiki page is retrieved from Redmine at $uri")
    public void resourcesAreRetrievedFromRedmine(String uri){
        LoadFromRedmine loadFromRedmine = new LoadFromRedmine(Type.JSON);
        text = loadFromRedmine.loadResourceAsText(uri);
    }

    @Then("the page contains the stories")
    public void pageContainsStories(){
        assertThat(text, containsString("Stories"));
    }

}
