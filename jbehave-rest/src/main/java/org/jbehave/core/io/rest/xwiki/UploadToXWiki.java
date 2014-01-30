package org.jbehave.core.io.rest.xwiki;

import static org.apache.commons.lang.StringUtils.substringAfterLast;

import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.UploadToREST;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;

/**
 * Uploads resource to XWiki pages using the REST API
 */
public class UploadToXWiki extends UploadToREST {

	public UploadToXWiki(Type type) {
		this(type, null, null);
	}

	public UploadToXWiki(Type type, String username, String password) {
		super(type, username, password);
	}

	protected String entity(String resourcePath, String text, Type type) {
		Page page = new Page();
		page.title = substringAfterLast(resourcePath, "/");
		page.syntax = "xwiki/2.0";
		page.content = text;
		switch (type) {
		case JSON:
			//TODO JSON upload does not seem to work
			Gson gson = new Gson();
			String json = gson.toJson(page);
			return json;
		case XML:
			page.xmlns = "http://www.xwiki.org";
			XStream xstream = new XStream();
			xstream.alias("page", Page.class);
			xstream.useAttributeFor(Page.class, "xmlns");
            xstream.aliasField("xmlns", Page.class, "xmlns");
            xstream.ignoreUnknownElements();
			String xml = xstream.toXML(page);
			return xml;
		default:
			return text;
		}
	}

	@SuppressWarnings("unused")
	private static class Page {
		private String xmlns;
		private String title;
		private String syntax;
		private String content;
	}

}
