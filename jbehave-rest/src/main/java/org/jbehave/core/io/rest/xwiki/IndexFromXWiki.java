package org.jbehave.core.io.rest.xwiki;

import static java.text.MessageFormat.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.io.rest.IndexWithBreadcrumbs;
import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceNameResolver;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Indexes resources from XWiki using the REST API
 */
public class IndexFromXWiki extends IndexWithBreadcrumbs {

	private static final String INDEX_URI = "{0}?media=json";
	private static final String PAGE_URI = "{0}/{1}";

	public IndexFromXWiki() {
		this(null, null);
	}

	public IndexFromXWiki(String username, String password) {
		this(username, password, new ToLowerCase());
	}

	public IndexFromXWiki(String username, String password, ResourceNameResolver nameResolver) {
		super(new RESTClient(Type.JSON, username, password), nameResolver);
	}

	protected Map<String, Resource> createIndexFromEntity(String rootURI, String entity) {
		Collection<Page> pages = parse(entity);
		Map<String, Resource> index = new HashMap<String, Resource>();
		for (Page page : pages) {
			String parentName = (page.parent != null ? resolveName(page.parent) : null);
			String uri = format(PAGE_URI, rootURI, page.name);
			Resource resource = new Resource(uri, resolveName(page.name), parentName);
			index.put(resource.getName(), resource);
		}
		return index;
	}

	protected String uri(String rootPath) {
		return format(INDEX_URI, rootPath);
	}

	private Collection<Page> parse(String entity) {
		Gson gson = new Gson();
		return gson.<Collection<Page>> fromJson(
				jsonMember(entity, "pageSummaries"),
				new TypeToken<Collection<Page>>() {
				}.getType());
	}

	private String jsonMember(String entity, String memberName) {
		return new JsonParser().parse(entity).getAsJsonObject().get(memberName)
				.toString();
	}

	private static class Page {
		private String name;
		private String parent;
	}

}
