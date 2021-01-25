package org.jbehave.core.io.rest.xwiki;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.junit.jupiter.api.Test;

public class LoadFromXWikiBehaviour {

	@Test
	public void canFormatURIForJSON() {
		LoadFromXWiki loader = new LoadFromXWiki(Type.JSON);
		String url = "http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/some_story";
		String uri = loader.uri(url, Type.JSON);
		assertThat(uri, equalTo(url+"?media=json"));
	}

	@Test
	public void canFormatURIForXML() {
		LoadFromXWiki loader = new LoadFromXWiki(Type.XML);
		String url = "http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/some_story";
		String uri = loader.uri(url, Type.XML);
		assertThat(uri, equalTo(url+"?media=xml"));
	}

	@Test
	public void canReadFromXWikiAsJSON() {
		LoadFromXWiki loader = new LoadFromXWiki(Type.JSON);
		String entity = read("xwiki.json");
		String text = loader.text(entity, Type.JSON);
		assertThat(text, startsWith("Narrative"));
	}

	@Test
	public void canReadFromXWikiAsXML() {
		LoadFromXWiki loader = new LoadFromXWiki(Type.XML);
		String entity = read("xwiki.xml");
		String text = loader.text(entity, Type.XML);
		assertThat(text, startsWith("Narrative"));
	}

	private String read(String path) {
		try {
			return IOUtils.toString(getClass().getClassLoader().getResource(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
