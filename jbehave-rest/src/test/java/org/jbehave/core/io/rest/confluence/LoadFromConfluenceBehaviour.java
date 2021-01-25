package org.jbehave.core.io.rest.confluence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.stringContainsInOrder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient;
import org.junit.jupiter.api.Test;

public class LoadFromConfluenceBehaviour {

    @Test
    public void shouldLoadFromConfluence() {
        // given
        LoadFromConfluence loader = new LoadFromConfluence(new MockRESTClient());

        // when
        String story = loader.loadResourceAsText("https://demo.confluence.com/rest/prototype/1/content/12517648");

        // then
        assertThat(story, startsWith("A story is a collection of scenarios"));
        assertThat(story, stringContainsInOrder(Arrays.asList(
                "Narrative:", "In order to communicate effectively to the business some functionality",
                "Lifecycle:", "Before:", "After:",
                "Scenario: A scenario is a collection of executable steps of different type",
                "Given a [precondition]", "When a negative event occurs", "Then a the outcome should [be-captured]",
                "Examples:", "|precondition|be-captured|", "|xyz|not be captured|")));
    }

    private static class MockRESTClient extends RESTClient {

        public MockRESTClient() {
            super(Type.XML);
        }

        @Override
        public String get(String uri) {
            return read("confluence-story.xml");
        }

        private String read(String path) {
            try {
                return IOUtils.toString(getClass().getClassLoader().getResource(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
