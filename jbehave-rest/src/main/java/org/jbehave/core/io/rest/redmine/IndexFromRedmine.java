package org.jbehave.core.io.rest.redmine;

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.io.rest.IndexWithBreadcrumbs;
import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Indexes resources from Redmine using the REST API
 */
public class IndexFromRedmine extends IndexWithBreadcrumbs {

    private static final String INDEX_URI = "{0}/index.json";
    private static final String PAGE_URI = "{0}/{1}";

    public IndexFromRedmine() {
        this(null, null);
    }

    public IndexFromRedmine(String username, String password) {
        super(new RESTClient(Type.JSON, username, password));
    }

    protected Map<String, Resource> createIndexFromEntity(String rootURI, String entity) {
    	Collection<Page> pages = parse(entity);
        Map<String, Resource> index = new HashMap<String, Resource>();
        for (Page page : pages) {
            String name = page.title.toLowerCase();
            String parentName = (page.parent != null ? page.parent.title.toLowerCase() : null);
            String uri = format(PAGE_URI, rootURI, name);
            Resource resource = new Resource(uri, name, parentName);
            index.put(name, resource);
        }
        return index;
    }

	protected String uri(String rootPath) {
		return format(INDEX_URI, rootPath);
	}

    private Collection<Page> parse(String entity) {
        Gson gson = new Gson();
        return gson.<Collection<Page>> fromJson(jsonMember(entity, "wiki_pages"),
                new TypeToken<Collection<Page>>() {
                }.getType());
    }

    private String jsonMember(String entity, String memberName) {
        return new JsonParser().parse(entity).getAsJsonObject().get(memberName).toString();
    }

    private static class Page {
        private String title;
        private Page parent;
    }

}
