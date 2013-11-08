package org.jbehave.core.rest;

import java.net.MalformedURLException;

import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.rest.LoadFromREST.Type;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;


public class LoadFromRESTBehaviour {

	private static final String URL = "http://www.redmine.org/projects/redmine/wiki/Themes";

	@Test
	public void canLoadFromRESTAsJSON() throws MalformedURLException {
		StoryLoader loadFromURL = new LoadFromREST(Type.JSON);
		String text = loadFromURL.loadStoryAsText(URL+".json");
		assertThat(text, containsString("wiki_page"));
	}

	@Test
	public void canLoadFromRESTAsXML() throws MalformedURLException {
		StoryLoader loadFromURL = new LoadFromREST(Type.XML);
		String text = loadFromURL.loadStoryAsText(URL+".xml");
        assertThat(text, containsString("wiki_page"));
	}
	
}
