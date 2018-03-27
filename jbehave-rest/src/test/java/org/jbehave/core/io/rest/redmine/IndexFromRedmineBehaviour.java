package org.jbehave.core.io.rest.redmine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.Resource;
import org.junit.Test;

public class IndexFromRedmineBehaviour {

    @Test
    public void canIndexFromRedmine() {
        IndexFromRedmine indexer = new IndexFromRedmine();
        String rootPath = "http://redmine.org/wiki";
        String entity = read("redmine-index.json");
        Map<String, Resource> index = indexer.createIndexFromEntity(rootPath, entity);
        assertThat(index.containsKey("a_story"), equalTo(true));
        assertThat(index.get("a_story").getURI(), equalTo(rootPath + "/A_story"));
        assertThat(index.containsKey("another_story"), equalTo(true));
        assertThat(index.get("another_story").getURI(), equalTo(rootPath + "/Another_story"));
    }

    private String read(String path) {
        try {
            return IOUtils.toString(getClass().getClassLoader().getResource(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
