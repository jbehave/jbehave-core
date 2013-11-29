package org.jbehave.core.io.rest.redmine;

import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.UploadToREST;

import com.google.gson.Gson;

import static java.text.MessageFormat.format;

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

    protected String entity(String text, Type type) {
        switch (type) {
        case JSON:
            Gson gson = new Gson();
            WikiPage page = new WikiPage();
            page.text = text;
            Entity entity = new Entity();
            entity.wiki_page = page;
            return gson.toJson(entity);
        case XML:
        default:
            return text;
        }
    }

    private static class Entity {
        private WikiPage wiki_page;
    }

    private static class WikiPage {
        private String text;
    }
    
}
