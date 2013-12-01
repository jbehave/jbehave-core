package org.jbehave.core.io.rest.redmine;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class IndexFromRedmineBehaviour {

    @Test
    public void canIndexFromRedmine() {
        ResourceIndexer indexer = new IndexFromRedmine();
        String rootPath = "http://redmine.org/wiki";
        String entity = read("redmine-index.xml");
        Map<String, Resource> index = indexer.indexResources(rootPath, entity);
        assertThat(index.size(), equalTo(2));
        assertThat(index.containsKey("A_story"), equalTo(true));
        assertThat(index.get("A_story").getURI(), equalTo(rootPath+"/A_story"));
        assertThat(index.containsKey("Another_story"), equalTo(true));
        assertThat(index.get("Another_story").getURI(), equalTo(rootPath+"/Another_story"));
    }

    private String read(String path) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
