package org.jbehave.core.io.rest.xwiki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.junit.jupiter.api.Test;

class UploadToXWikiBehaviour {

	@Test
	void canFormatAsJSONWithDefaultSyntax() {
	    UploadToXWiki uploader = new UploadToXWiki(Type.JSON);
		String resourcePath = "http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/some_story";
		String content = read("xwiki.json");
		Resource resource = new Resource(resourcePath);
		resource.setContent(content);
		String entity = uploader.entity(resource, Type.JSON);
		assertThat(entity, containsString("\"title\":\"some_story\""));
		assertThat(entity, containsString("\"syntax\":\"xwiki/2.0\""));
	}

	@Test
	void canFormatAsJSONWithGivenSyntax() {
	    UploadToXWiki uploader = new UploadToXWiki(Type.JSON);
		String resourcePath = "http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/some_story";
		String content = read("xwiki.json");
		Resource resource = new Resource(resourcePath);
		resource.setContent(content);
		resource.setSyntax("jbehave/3.0");
		String entity = uploader.entity(resource, Type.JSON);
		assertThat(entity, containsString("\"title\":\"some_story\""));
		assertThat(entity, containsString("\"syntax\":\"jbehave/3.0\""));
	}

	private String read(String path) {
		try {
			return IOUtils.toString(getClass().getClassLoader().getResource(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
