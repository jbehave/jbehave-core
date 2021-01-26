package org.jbehave.core.io.rest.xwiki;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
            return IOUtils.toString(getClass().getClassLoader().getResource(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
