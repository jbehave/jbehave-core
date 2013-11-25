package org.jbehave.examples.core.rest.steps;

import java.util.Map;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.redmine.IndexFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class RESTSteps {

    private String text;
    private Map<String,Resource> index;

    @When("index is retrieved from Redmine at $uri")
    public void indexIsRetrievedFromRedmine(String uri){
        ResourceIndexer indexer = new IndexFromRedmine();
        index = indexer.indexResources(uri);        
    }
    
    @Then("the index contains $number stories")
    public void indexContainsStories(int number){
        assertThat(index.size(), equalTo(number));
    }

    @When("story $name is loaded from Redmine")
    public void storyIsLoadedFromRedmine(String name){
        ResourceLoader loader = new LoadFromRedmine(Type.JSON);
        text = loader.loadResourceAsText(index.get(name).getURI());
    }

    @Then("story contains title '$title'")
    public void storyContainsTitle(String title){
        assertThat(text, containsString(title));
    }

}
