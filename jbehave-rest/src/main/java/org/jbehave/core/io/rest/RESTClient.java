package org.jbehave.core.io.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import static java.text.MessageFormat.format;

/**
 *  Provides access to REST resources
 */
public class RESTClient {

	public enum Type { JSON, XML };
	
	private static final String APPLICATION_TYPE = "application/{0}";
	private String username;
	private String password;
	private Type type;

	public RESTClient(Type type) {
		this(type, null, null);
	}

	public RESTClient(Type type, String username, String password) {
		this.type = type;
		this.username = username;
		this.password = password;
	}

    public Type getType() {
        return type;
    }

	public String get(String uri) {
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
