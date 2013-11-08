package org.jbehave.core.rest;

import static java.text.MessageFormat.format;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.XStream;

/**
 * Loads story resources from Redmine wiki pages using the REST API
 */
public class LoadFromRedmine extends LoadFromREST {

	private static final String REDMINE_URI = "{0}.{1}";

	public LoadFromRedmine(Type type) {
		super(type);
	}

	public LoadFromRedmine(Type type, String username, String password) {
		super(type, username, password);
	}

	protected String uri(String resourcePath, Type type) {
		return format(REDMINE_URI, resourcePath, type.name().toLowerCase());
	}

	protected String text(String entity, Type type) {
		switch (type) {
		case JSON:
			Gson gson = new Gson();
			return gson.fromJson(jsonMember(entity, "wiki_page"),
					WikiPage.class).text;
		case XML:
			XStream xstream = new XStream();
			xstream.alias("wiki_page", WikiPage.class);
			xstream.ignoreUnknownElements();
			return ((WikiPage) xstream.fromXML(entity)).text;
		default:
			return entity;
		}
	}

	private String jsonMember(String entity, String memberName) {
		return new JsonParser().parse(entity).getAsJsonObject().get(memberName)
				.toString();
	}

	private static class WikiPage {
		String text;
	}
}
