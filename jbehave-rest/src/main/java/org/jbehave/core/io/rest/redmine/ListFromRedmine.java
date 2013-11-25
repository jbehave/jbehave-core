package org.jbehave.core.io.rest.redmine;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;

import com.thoughtworks.xstream.XStream;

import static java.text.MessageFormat.format;

/**
 * List story resources from Redmine wiki pages using the REST API
 */
public class ListFromRedmine {

    private static final String INDEX_URI = "{0}/index.xml";
    private static final String PAGE_URI = "{0}/{1}";

    private RESTClient client;

    public ListFromRedmine() {
        this(null, null);
    }

    public ListFromRedmine(String username, String password) {
        this.client = new RESTClient(Type.XML, username, password);
    }

    public List<String> listResources(String rootPath) {
        try {
            return list(entity(uri(rootPath)), rootPath);
        } catch (Exception cause) {
            throw new InvalidStoryResource(rootPath, cause);
        }
    }

    private String entity(String uri) {
        return client.get(uri);
    }

    private String uri(String rootPath) {
        return format(INDEX_URI, rootPath);
    }

    protected List<String> list(String entity, String rootPath) {
        XStream xstream = new XStream();
        xstream.alias("wiki_pages", WikiPages.class);
        xstream.alias("wiki_page", WikiPage.class);
        xstream.addImplicitCollection(WikiPages.class, "wiki_pages");
        xstream.ignoreUnknownElements();
        WikiPages pages = ((WikiPages) xstream.fromXML(entity));
        List<String> list = new ArrayList<String>();
        for (WikiPage page : pages.wiki_pages) {
            if ( page.parent != null ){
                // only include pages with parent to exclude the root page
                list.add(format(PAGE_URI, rootPath, page.title));
            }
        }
        return list;
    }

    private static class WikiPages {
        List<WikiPage> wiki_pages;
    }

    private static class WikiPage {
        private String title;
        private String parent;
    }

}
