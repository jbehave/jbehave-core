package org.jbehave.core.io.rest;

import static org.apache.commons.lang.StringUtils.substringAfterLast;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a resource retrieved from a REST API.
 */
public class Resource {

    private final String uri;
    private final String name;
    private final String parentName;
    private String text;
    private String breadcrumbs;

    public Resource(String uri) {
        this(uri, substringAfterLast(uri, "/"));
    }

    public Resource(String uri, String name) {
        this(uri, name, null);
    }

    public Resource(String uri, String name, String parentName) {
        this.uri = uri;
        this.name = name;
        this.parentName = parentName;
    }

    public String getURI() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getParentName() {
        return parentName;
    }

    public boolean hasParent() {
        return parentName != null;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean hasText() {
        return text != null;
    }

    public String getBreadcrumbs() {
        return breadcrumbs;
    }

    public void setBreadcrumbs(String breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
    }

    public boolean hasBreadcrumbs() {
        return breadcrumbs != null;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
