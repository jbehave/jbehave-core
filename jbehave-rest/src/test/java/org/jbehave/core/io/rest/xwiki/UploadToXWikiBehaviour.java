package org.jbehave.core.io.rest.xwiki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.junit.Test;

public class UploadToXWikiBehaviour {

	@Test
	public void canFormatAsJSONWithDefaultSyntax() {
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
	public void canFormatAsJSONWithGivenSyntax() {
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
			return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
