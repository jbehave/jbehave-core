package org.jbehave.core.io.rest.xwiki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.junit.Test;

public class UploadToXWikiBehaviour {

	@Test
	public void canFormatAsJSON() {
	    UploadToXWiki uploader = new UploadToXWiki(Type.JSON);
		String resourcePath = "http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/some_story";
		String text = read("xwiki.json");
		String entity = uploader.entity(resourcePath, text, Type.JSON);
		assertThat(entity, startsWith("{\"title\""));
	}

	private String read(String path) {
		try {
			return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
