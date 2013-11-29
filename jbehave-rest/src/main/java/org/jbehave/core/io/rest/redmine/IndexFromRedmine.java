package org.jbehave.core.io.rest.redmine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceIndexer;

import com.thoughtworks.xstream.XStream;

import static java.text.MessageFormat.format;

/**
 * Indexes resources from Redmine using the REST API
 */
public class IndexFromRedmine implements ResourceIndexer {

    private static final String INDEX_URI = "{0}/index.xml";
    private static final String PAGE_URI = "{0}/{1}";

    private RESTClient client;

    public IndexFromRedmine() {
        this(null, null);
    }

    public IndexFromRedmine(String username, String password) {
        this.client = new RESTClient(Type.XML, username, password);
    }

    public Map<String,Resource> indexResources(String rootURI) {
        try {
            return index(entity(uri(rootURI)), rootURI);
        } catch (Exception cause) {
            throw new InvalidStoryResource(rootURI, cause);
        }
    }

    private String entity(String uri) {
        return client.get(uri);
    }

    private String uri(String rootPath) {
        return format(INDEX_URI, rootPath);
    }

    protected Map<String, Resource> index(String entity, String rootURI) {
        XStream xstream = new XStream();
        xstream.alias("wiki_pages", WikiPages.class);
        xstream.alias("wiki_page", WikiPage.class);
        xstream.addImplicitCollection(WikiPages.class, "wiki_pages");
        xstream.ignoreUnknownElements();
        WikiPages pages = ((WikiPages) xstream.fromXML(entity));
        Map<String, Resource> index = new HashMap<String, Resource>();
        for (WikiPage page : pages.wiki_pages) {
            if ( page.parent != null ){
                // only include pages with parent to exclude the root page
                String name = page.title;
                index.put(name, new Resource(name, format(PAGE_URI, rootURI, name)));
            }
        }
        return index;
    }

    private static class WikiPages {
        List<WikiPage> wiki_pages;
    }

    private static class WikiPage {
        private String title;
        private String parent;
    }

}
