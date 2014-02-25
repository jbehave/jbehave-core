package org.jbehave.core.io.rest.confluence;

import static java.text.MessageFormat.format;

import java.util.List;

import org.jbehave.core.io.rest.RESTClient;

import com.thoughtworks.xstream.XStream;

public class Confluence {

    private static final String SEARCH_PAGE = "{0}/rest/prototype/1/search/name?query={2}&type=page&spaceKey={1}";
    private static final String EXPAND_PAGE = "{0}?expand=children";
    private static final String REGULAR_PAGE = "{0}";

    private final RESTClient client;

    public Confluence(RESTClient client) {
        this.client = client;
    }

    public Page loadRootPage(String baseUrl, String spaceKey, String pageName) {
        String searchResult = client.get(format(SEARCH_PAGE, baseUrl, spaceKey, pageName));
        XStream parse = configureXStream();
        Results results = (Results) parse.fromXML(searchResult);
        return results.getGroup().getResult();
    }

    public Page loadPage(String pageUrl, boolean expanded) {
        String pattern = expanded ? EXPAND_PAGE : REGULAR_PAGE;
        String content = client.get(format(pattern, pageUrl));
        XStream parse = configureXStream();
        Page page = (Page) parse.fromXML(content);
        return page;
    }

    protected XStream configureXStream() {
        XStream stream = new XStream();
        stream.addImplicitCollection(Page.class, "link");
        stream.alias("results", Results.class);
        stream.alias("result", Page.class);
        stream.alias("content", Page.class);
        stream.alias("link", Link.class);
        stream.useAttributeFor(Link.class, "rel");
        stream.useAttributeFor(Link.class, "href");
        stream.ignoreUnknownElements();
        return stream;
    }

    public static class Results {

        private Group group;

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }

    }

    public static class Group {

        private Page result;

        public Page getResult() {
            return result;
        }

        public void setResult(Page result) {
            this.result = result;
        }

    }

    public static class Page {

        private List<Link> link;
        private String title;
        private String body;
        private List<Page> children;

        public String getSelfReference() {
            for (Link candidate : link) {
                if ("self".equals(candidate.getRel())) {
                    return candidate.getHref();
                }
            }
            throw new RuntimeException("Page does not contain self-reference");
        }

        public boolean hasChildren() {
            return children != null;
        }

        public List<Link> getLink() {
            return link;
        }

        public void setLink(List<Link> link) {
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Page> getChildren() {
            return children;
        }

        public void setChildren(List<Page> children) {
            this.children = children;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

    }

    public static class Link {

        private String rel;
        private String href;

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

    }
}
