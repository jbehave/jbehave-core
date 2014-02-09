package org.jbehave.core.io.rest;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.rest.RESTClient.Type;

/**
 * Uploads resource to REST
 */
public class UploadToREST implements ResourceUploader {

    private RESTClient client; 
    
    public UploadToREST(Type type) {
        this(type, null, null);
    }

    public UploadToREST(Type type, String username, String password) {
        this.client = new RESTClient(type, username, password);
    }
    
    public UploadToREST(RESTClient client) {
        this.client = client;
    }
    
    public void uploadResource(Resource resource) {
        try {
            Type type = client.getType();
            String resourcePath = resource.getURI();
			put(uri(resourcePath, type), entity(resource, type));
        } catch (Exception cause) {
            throw new InvalidStoryResource(resource.toString(), cause);
        }
    }

	protected String uri(String resourcePath, Type type) {
		return resourcePath;
	}

	protected String entity(Resource resource, Type type) {
		return resource.getText();
	}

	private void put(String uri, String entity) {
		client.put(uri, entity);
	}

}
