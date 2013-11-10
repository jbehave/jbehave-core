package org.jbehave.core.io.rest;

import java.net.MalformedURLException;

import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LoadFromRESTBehaviour {

	@Test
	public void canLoadFromREST() throws MalformedURLException {
	    RESTClient client = mock(RESTClient.class);
	    when(client.getType()).thenReturn(Type.JSON);
	    String url = "http://wiki/page";
        String entity = "Some content";
        when(client.get(url)).thenReturn(entity);
		StoryLoader loadFromURL = new LoadFromREST(client);
		String text = loadFromURL.loadStoryAsText(url);
		assertThat(text, containsString(entity));
	}

}
