package org.jbehave.examples.google.steps;

import java.io.IOException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.google.LoadOdtFromGoogle;

public class GoogleSteps {

    private StoryLoader storyLoader;
    private String storyAsText;

    @Given("Google feed $uri")
    public void givenGoogleFeed(String feedURI) {
        String username = System.getenv("GOOGLE_USER");
        String password = System.getenv("GOOGLE_PASSWORD");
        storyLoader = new LoadOdtFromGoogle(username, password, feedURI);
    }

    @When("story $storyPath is loaded from feed")
    public void whenStoryIsLoadedFromFeed(String storyPath) {
        storyAsText = storyLoader.loadStoryAsText(storyPath).trim();
    }

    @Then("content is same as $storyPath")
    public void thenContentIsSameAs(String storyPath) throws IOException {
        String expected = new LoadFromClasspath(this.getClass()).loadResourceAsText(storyPath).trim();
        MatcherAssert.assertThat(storyAsText, Matchers.equalTo(expected));
    }
}
