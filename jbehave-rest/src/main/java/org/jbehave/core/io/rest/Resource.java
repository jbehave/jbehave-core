package org.jbehave.core.io.rest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a resource retrieved from a REST API.
 */
public class Resource {

    private final String name;
    private final String uri;
    private String text;

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

    public void setText(String text) {
        this.text = text;        
    }
    
    public String getText(){
        return text;
    }

    public boolean hasText(){
        return text != null;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
