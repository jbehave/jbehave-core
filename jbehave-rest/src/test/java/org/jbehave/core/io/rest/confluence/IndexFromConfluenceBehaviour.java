package org.jbehave.core.io.rest.confluence;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.Resource;
import org.junit.Test;

public class IndexFromConfluenceBehaviour {

    @Test
    public void should_index_from_confluence() {
        // given
        IndexFromConfluence indexer = new IndexFromConfluence(new MockRESTClient());

        // when
        Map<String, Resource> index = indexer.indexResources("https://demo.confluence.com/display/JBEV/jBehave");

        // then
        assertEquals(2, index.size());
    }

    private static class MockRESTClient extends RESTClient {

        public MockRESTClient() {
            super(Type.XML);
        }

        @Override
        public String get(String uri) {
            if (uri.contains("search")) {
                return read("confluence-search.xml");
            }
            if (uri.contains("12517648")) {
                return read("confluence-story-expanded.xml");
            }
            return read("confluence-story.xml");
        }

        private String read(String path) {
            try {
                return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
