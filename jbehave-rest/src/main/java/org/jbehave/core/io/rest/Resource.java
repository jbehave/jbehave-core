package org.jbehave.core.io.rest;

public class Resource {

    private String name;
    private String uri;

    public Resource(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getURI() {
        return uri;
    }

}
