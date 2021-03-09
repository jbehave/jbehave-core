package org.jbehave.core.io.rest.confluence;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.confluence.Confluence.Page;

public class IndexFromConfluence implements ResourceIndexer {

    private static final String DISPLAY = "/display/";

    private final Confluence confluence;

    public IndexFromConfluence() {
        this(null, null);
    }

    public IndexFromConfluence(String username, String password) {
        this(new RESTClient(Type.XML, username, password));
    }

    public IndexFromConfluence(RESTClient client) {
        this.confluence = new Confluence(client);
    }

    @Override
    public Map<String, Resource> indexResources(String rootURI) {
        return indexResources(rootURI, null);
    }

    @Override
    public Map<String, Resource> indexResources(String rootURI, String rootPath, String syntax, String includes) {
        return indexResources(rootURI, includes);
    }

    protected Map<String, Resource> indexResources(String rootURI, String includePattern) {
        if (rootURI == null || !rootURI.contains(DISPLAY)) {
            throw new RuntimeException("Root URI is not in correct format: " + rootURI);
        }
        String[] split = rootURI.split(DISPLAY);
        String baseUrl = split[0];
        if (split.length == 1) {
            throw new RuntimeException("URI does not contain space and page: " + rootURI);
        }
        String[] searchTerms = split[1].split("/");
        if (split.length != 2) {
            throw new RuntimeException("URI does not contain space and page: " + rootURI);
        }
        return createResourceMap(baseUrl, searchTerms[0], searchTerms[1], includePattern);
    }

    private Map<String, Resource> createResourceMap(String baseUrl, String spaceKey, String pageName, String pattern) {
        Map<String, Resource> result = new HashMap<>();
        Page rootPage = confluence.loadRootPage(baseUrl, spaceKey, pageName);
        addPage(result, rootPage.getSelfReference(), pattern);
        return result;
    }

    private void addPage(Map<String, Resource> result, String href, String pattern) {
        Page page = confluence.loadPage(href, true);
        Resource resource = new Resource(page.getSelfReference(), page.getTitle());
        resource.setContent(page.getBody());
        if (pattern == null || (pattern != null && Pattern.matches(pattern, page.getTitle()))) {
            result.put(page.getTitle(), resource);
        }
        if (page.hasChildren()) {
            for (Page child : page.getChildren()) {
                addPage(result, child.getSelfReference(), pattern);
            }
        }
    }

}
