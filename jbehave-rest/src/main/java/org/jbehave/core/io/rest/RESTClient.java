package org.jbehave.core.io.rest;

import static java.text.MessageFormat.format;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;

/**
 * Provides access to REST resources
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class RESTClient {

    public enum Type {
        JSON, XML
    }

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
        return client().target(uri).request(mediaType(type))
                .get(ClientResponse.class).getEntity().toString();
    }

    public void put(String uri, String entity) {
        client().target(uri).request(mediaType(type))
                .put(Entity.entity(entity, mediaType(type)));
    }

    private String mediaType(Type type) {
        return format(APPLICATION_TYPE, type.name().toLowerCase());
    }

    private Client client() {
        ClientConfig clientConfig = new ClientConfig();
        if (username != null) {
            clientConfig.register(HttpAuthenticationFeature.basic(username, password));
        }
        return ClientBuilder.newClient(clientConfig);
    }

}
