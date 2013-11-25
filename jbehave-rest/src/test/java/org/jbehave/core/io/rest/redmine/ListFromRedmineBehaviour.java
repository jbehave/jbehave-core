package org.jbehave.core.io.rest.redmine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class ListFromRedmineBehaviour {

    @Test
    public void canListFromRedmineAsXML() {
        ListFromRedmine loader = new ListFromRedmine();
        String entity = read("redmine-index.xml");
        String rootPath = "http://redmine.org/wiki";
        List<String> list = loader.list(entity, rootPath);
        assertThat(list, equalTo(Arrays.asList(rootPath + "/Another_story", rootPath + "/A_story")));
    }

    private String read(String path) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
