package org.jbehave.core.io.rest.redmine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

public class LoadFromRedmineBehaviour {

	@Test
	public void canFormatURIForJSON() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.JSON);
		String url = "http://demo.redmine.org/project/jbehave/wiki/some_story";
		String text = loader.uri(url, Type.JSON);
		assertThat(text, equalTo(url+".json"));
	}

	@Test
	public void canReadFromRedmineAsJSON() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.JSON);
		String entity = read("redmine.json");
		String text = loader.text(entity, Type.JSON);
		assertThat(text, startsWith("Narrative"));
	}

	@Test
	public void canFormatURIForXML() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.XML);
		String url = "http://demo.redmine.org/project/jbehave/wiki/some_story";
		String text = loader.uri(url, Type.XML);
		assertThat(text, equalTo(url+".xml"));
	}

	@Test
	public void canReadFromRedmineAsXML() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.XML);
		String entity = read("redmine.xml");
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
