package org.jbehave.core.io.rest.xwiki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.Resource;
import org.junit.Test;

public class IndexFromXWikiBehaviour {

    @Test
    public void canIndexFromXWiki() {
    	IndexFromXWiki indexer = new IndexFromXWiki();
        String rootPath = "http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages";
        String entity = read("xwiki-index.json");
        Map<String, Resource> index = indexer.createIndexFromEntity(rootPath, entity);
        assertThat(index.containsKey("a_story"), equalTo(true));
        assertThat(index.get("a_story").getURI(), equalTo(rootPath + "/a_story"));
        assertThat(index.containsKey("another_story"), equalTo(true));
        assertThat(index.get("another_story").getURI(), equalTo(rootPath + "/another_story"));
    }

    private String read(String path) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
