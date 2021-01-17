package org.jbehave.core.io.rest.redmine;

import com.thoughtworks.xstream.security.AnyTypePermission;
import org.jbehave.core.io.rest.LoadFromREST;
import org.jbehave.core.io.rest.RESTClient.Type;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.XStream;

import static java.text.MessageFormat.format;

/**
 * Loads resource from Redmine wiki pages using the REST API
 */
public class LoadFromRedmine extends LoadFromREST {

	private static final String REDMINE_URI = "{0}.{1}";

	public LoadFromRedmine(Type type) {
		this(type, null, null);
	}

	public LoadFromRedmine(Type type, String username, String password) {
		super(type, username, password);
	}

	@Override
    protected String uri(String resourcePath, Type type) {
		return format(REDMINE_URI, resourcePath, type.name().toLowerCase());
	}

	@Override
    protected String text(String entity, Type type) {
		switch (type) {
		case JSON:
			Gson gson = new Gson();
			return gson.fromJson(jsonMember(entity, "wiki_page"),
					WikiPage.class).text;
		case XML:
			XStream xstream = new XStream();
			XStream.setupDefaultSecurity(xstream);
			xstream.addPermission(AnyTypePermission.ANY);
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
