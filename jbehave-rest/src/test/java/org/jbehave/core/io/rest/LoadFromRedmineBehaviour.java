package org.jbehave.core.io.rest;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

public class LoadFromRedmineBehaviour {

	@Test
	public void canFormatURIForJSON() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.JSON);
		String url = "http://redmine/project/bdd/wiki/stories/some_story";
		String text = loader.uri(url, Type.JSON);
		assertThat(text, equalTo(url+".json"));
	}

	@Test
	public void canLoadFromRedmineAsJSON() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.JSON);
		String entity = read("redmine.json");
		String text = loader.text(entity, Type.JSON);
		assertThat(text, startsWith("Narrative"));
	}

	@Test
	public void canFormatURIForXML() {
		LoadFromRedmine loader = new LoadFromRedmine(Type.XML);
		String url = "http://redmine/project/bdd/wiki/stories/some_story";
		String text = loader.uri(url, Type.XML);
		assertThat(text, equalTo(url+".xml"));
	}

	@Test
	public void canLoadFromRedmineAsXML() throws MalformedURLException {
		LoadFromRedmine loader = new LoadFromRedmine(Type.XML);
		String entity = read("redmine.xml");
		String text = loader.text(entity, Type.XML);
		assertThat(text, startsWith("Narrative"));
	}

	private String read(String path) {
		try {
			return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
