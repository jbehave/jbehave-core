package org.jbehave.core.io.rest.redmine;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceIndexer;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Indexes resources from Redmine using the REST API
 */
public class IndexFromRedmine implements ResourceIndexer {

    private static final String INDEX_URI = "{0}/index.json";
    private static final String PAGE_URI = "{0}/{1}";

    private RESTClient client;

    public IndexFromRedmine() {
        this(null, null);
    }

    public IndexFromRedmine(String username, String password) {
        this.client = new RESTClient(Type.JSON, username, password);
    }

    public Map<String, Resource> indexResources(String rootURI) {
        return indexResources(rootURI, entity(uri(rootURI)));
    }

    public Map<String, Resource> indexResources(String rootURI, String entity) {
        Map<String, Resource> index = createIndex(rootURI, parse(entity));
        addBreadcrumbs(index);
        return index;
    }

    private void addBreadcrumbs(Map<String, Resource> index) {
        for (Resource resource : index.values()) {
            List<String> breadcrumbs = new ArrayList<String>();
            collectBreadcrumbs(breadcrumbs, resource, index);
            if (!breadcrumbs.isEmpty()) {
                resource.setBreadcrumbs(join(breadcrumbs, "/"));
            }
        }
    }

    private void collectBreadcrumbs(List<String> breadcrumbs, Resource resource, Map<String, Resource> index) {
        if (resource.hasParent()) {
            String parentName = resource.getParentName();
            breadcrumbs.add(0, parentName);
            Resource parent = index.get(parentName);
            if (parent != null) {
                collectBreadcrumbs(breadcrumbs, parent, index);
            }
        }
    }

    private Map<String, Resource> createIndex(String rootURI, Collection<WikiPage> pages) {
        Map<String, Resource> index = new HashMap<String, Resource>();
        for (WikiPage page : pages) {
            String name = page.title;
            String parentName = (page.parent != null ? page.parent.title : null);
            String uri = format(PAGE_URI, rootURI, name);
            Resource resource = new Resource(uri, name, parentName);
            index.put(name, resource);
        }
        return index;
    }

    private Collection<WikiPage> parse(String entity) {
        Gson gson = new Gson();
        return gson.<Collection<WikiPage>> fromJson(jsonMember(entity, "wiki_pages"),
                new TypeToken<Collection<WikiPage>>() {
                }.getType());
    }

    private String jsonMember(String entity, String memberName) {
        return new JsonParser().parse(entity).getAsJsonObject().get(memberName).toString();
    }

    private String entity(String uri) {
        return client.get(uri);
    }

    private String uri(String rootPath) {
        return format(INDEX_URI, rootPath);
    }

    private static class WikiPage {
        private String title;
        private WikiPage parent;
    }

}
