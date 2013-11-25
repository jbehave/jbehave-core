package org.jbehave.examples.core.rest.steps;

import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.rest.ResourceLister;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.redmine.ListFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;

public class RESTSteps {

    private String text;
    private List<String> list;

    @When("index is retrieved from Redmine at $uri")
    public void indexIsRetrievedFromRedmine(String uri){
        ResourceLister loadFromRedmine = new ListFromRedmine();
        list = loadFromRedmine.listResources(uri);        
    }
    
    @Then("the index contains $number stories")
    public void indexContainsStories(int number){
        assertThat(list.size(), Matchers.equalTo(number));
    }

    @When("story $title is loaded from Redmine")
    public void storyIsLoadedFromRedmine(String title){
        LoadFromRedmine loadFromRedmine = new LoadFromRedmine(Type.JSON);
        text = loadFromRedmine.loadResourceAsText(findURI(title));
    }

    private String findURI(String title) {
        for ( String uri : list ){
            if ( uri.endsWith(title) ){
                return uri;
            }
        }
        throw new RuntimeException("No uri found with tile "+title);
    }

    @Then("story contains title '$title'")
    public void storyContainsTitle(String title){
        assertThat(text, containsString(title));
    }

}
