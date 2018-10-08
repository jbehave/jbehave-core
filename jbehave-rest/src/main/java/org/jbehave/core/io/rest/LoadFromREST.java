package org.jbehave.core.io.rest;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient.Type;

/**
 * Loads resource from REST
 */
public class LoadFromREST implements ResourceLoader {

    private RESTClient client; 
    
    public LoadFromREST(Type type) {
        this(type, null, null);
    }

    public LoadFromREST(Type type, String username, String password) {
        this.client = new RESTClient(type, username, password);
    }
    
    public LoadFromREST(RESTClient client) {
        this.client = client;
    }
    
    @Override
    public String loadResourceAsText(String resourcePath) {
		try {
			Type type = client.getType();
            return text(get(uri(resourcePath, type)), type);
		} catch (Exception cause) {
			throw new InvalidStoryResource(resourcePath, cause);
		}
	}

	protected String uri(String resourcePath, Type type) {
		return resourcePath;
	}

	protected String text(String entity, Type type) {
		return entity;
	}

	private String get(String uri) {
		return client.get(uri);
	}

}
