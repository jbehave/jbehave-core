package org.jbehave.core.io.rest.redmine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.junit.jupiter.api.Test;

class UploadToRedmineBehaviour {

    @Test
    void canFormatURIForJSON() {
        UploadToRedmine uploader = new UploadToRedmine(Type.JSON);
        String resourcePath = "http://demo.redmine.org/project/jbehave/wiki/some_story";
        String text = uploader.uri(resourcePath, Type.JSON);
        assertThat(text, equalTo(resourcePath + ".json"));
    }

    @Test
    void canFormatAsJSON() {
        UploadToRedmine uploader = new UploadToRedmine(Type.JSON);
        String resourcePath = "http://demo.redmine.org/project/jbehave/wiki/some_story";
        String text = read("redmine.json");
        Resource resource = new Resource(resourcePath);
        resource.setContent(text);
        String entity = uploader.entity(resource, Type.JSON);
        assertThat(entity, startsWith("{\"wiki_page\""));
    }

    private String read(String path) {
        try {
            return IOUtils.toString(getClass().getClassLoader().getResource(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
