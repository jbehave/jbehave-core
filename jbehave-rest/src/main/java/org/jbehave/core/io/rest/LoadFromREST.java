package org.jbehave.core.io.rest;

import static java.text.MessageFormat.format;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.StoryLoader;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Loads story resources from REST
 */
public class LoadFromREST implements StoryLoader, ResourceLoader {

	public enum Type { JSON, XML };
	
	private static final String APPLICATION_TYPE = "application/{0}";
	private String username;
	private String password;
	private Type type;

	public LoadFromREST(Type type) {
		this(type, null, null);
	}

	public LoadFromREST(Type type, String username, String password) {
		this.type = type;
		this.username = username;
		this.password = password;
	}

	public String loadResourceAsText(String resourcePath) {
		try {
			return text(entity(uri(resourcePath, type)), type);
		} catch (Exception cause) {
			throw new InvalidStoryResource(resourcePath, cause);
		}
	}

	public String loadStoryAsText(String storyPath) {
		return loadResourceAsText(storyPath);
	}

	protected String uri(String resourcePath, Type type) {
		return resourcePath;
	}

	protected String text(String entity, Type type) {
		return entity;
	}

	private String entity(String uri) {
		return client().resource(uri).accept(format(APPLICATION_TYPE, type.name().toLowerCase()))
				.get(ClientResponse.class).getEntity(String.class);
	}

	private Client client() {
		Client client = Client.create();
		if (username != null) {
			client.addFilter(new HTTPBasicAuthFilter(username, password));
		}
		return client;
	}
}
