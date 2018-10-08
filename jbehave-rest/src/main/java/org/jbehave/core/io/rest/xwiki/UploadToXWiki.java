package org.jbehave.core.io.rest.xwiki;

import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
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

	@Override
    protected String entity(Resource resource, Type type) {
		Page page = new Page();
		page.syntax = ( resource.hasSyntax() ? resource.getSyntax() : "xwiki/2.0");
		page.title = resource.getName();
		page.content = resource.getContent();
		page.parent = resource.getParentName();
		switch (type) {
		case JSON:
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
			return resource.getContent();
		}
	}

	@SuppressWarnings("unused")
	private static class Page {
		private String xmlns;
		private String title;
		private String syntax;
		private String content;
		private String parent;
	}

}
