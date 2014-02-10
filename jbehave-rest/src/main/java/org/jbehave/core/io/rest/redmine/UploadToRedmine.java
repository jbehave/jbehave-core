package org.jbehave.core.io.rest.redmine;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.substringAfterLast;

import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.UploadToREST;

import com.google.gson.Gson;

/**
 * Uploads resource to Redmine wiki pages using the REST API
 */
public class UploadToRedmine extends UploadToREST {

	private static final String REDMINE_URI = "{0}.{1}";

	public UploadToRedmine(Type type) {
		this(type, null, null);
	}

	public UploadToRedmine(Type type, String username, String password) {
		super(type, username, password);
	}

	protected String uri(String resourcePath, Type type) {
		return format(REDMINE_URI, resourcePath, type.name().toLowerCase());
	}

	protected String entity(Resource resource, Type type) {
		Page page = new Page();
		page.title = substringAfterLast(resource.getURI(), "/");
		page.text = resource.getContent();
		Entity entity = new Entity();
		entity.wiki_page = page;
		switch (type) {
		case JSON:
			Gson gson = new Gson();
			return gson.toJson(entity);
		case XML:
		default:
			return resource.getContent();
		}
	}

	private static class Entity {
		private Page wiki_page;
	}

	private static class Page {
		private String title;
		private String text;
	}

}
