package org.jbehave.core.io.rest;

import static org.apache.commons.lang.StringUtils.substringAfterLast;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a resource retrieved from a REST API.
 */
public class Resource {

    private final String uri;
    private final String name;
    private final String parentName;
    private List<String> breadcrumbs;
    private String content;

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

    public List<String> getBreadcrumbs() {
        return breadcrumbs;
    }

	public void setBreadcrumbs(List<String> breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
    }

    public boolean hasBreadcrumbs() {
        return breadcrumbs != null && !breadcrumbs.isEmpty();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
