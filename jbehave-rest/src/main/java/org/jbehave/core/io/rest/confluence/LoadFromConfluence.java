package org.jbehave.core.io.rest.confluence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.confluence.Confluence.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class LoadFromConfluence implements ResourceLoader {

    private final Confluence confluence;
    private final Set<String> acceptedMacros = new HashSet<String>(Arrays.asList("panel", "info"));

    public LoadFromConfluence() {
        this(null, null);
    }

    public LoadFromConfluence(String username, String password) {
        this(new RESTClient(Type.XML, username, password));
    }

    public LoadFromConfluence(RESTClient client) {
        confluence = new Confluence(client);
    }

    public String loadResourceAsText(String resourcePath) {
        Page page = confluence.loadPage(resourcePath, false);
        Document doc = Jsoup.parse(page.getBody());
        StringBuilder builder = new StringBuilder();
        addTitle(doc, builder);
        addPanels(doc, builder);
        addExamples(doc, builder);
        return builder.toString();
    }

    protected void addTitle(Document doc, StringBuilder builder) {
        Element titleEl = doc.getElementsByTag("h1").first();
        String title = titleEl.text();
        builder.append(title).append("\n\n");
    }

    protected void addPanels(Document doc, StringBuilder builder) {
        Elements elements = doc.getElementsByTag("ac:structured-macro");
        for (Element element : elements) {
            String name = element.attr("ac:name");
            if (acceptedMacros.contains(name)) {
                appendMacroTitle(builder, element);
                appendMacroBody(builder, element);
            }
        }
    }

    private void appendMacroTitle(StringBuilder builder, Element element) {
        Elements parameters = element.getElementsByTag("ac:parameter");
        if (parameters.size() > 0) {
            for (Element param : parameters) {
                if ("title".equals(param.attr("ac:name"))) {
                    String text = param.text();
                    if (!text.contains(":")) {
                        text = text + ":";
                    }
                    builder.append(text).append("\n");
                }
            }
        }
    }

    private void appendMacroBody(StringBuilder builder, Element element) {
        Elements bodies = element.getElementsByTag("ac:rich-text-body");
        if (!bodies.isEmpty()) {
            Element body = bodies.first();
            cleanNodes(body, "div");
            cleanNodes(body, "p");
            builder.append(body.text().replaceAll("<br/>", "\n")).append("\n");
        }
    }

    protected void addExamples(Document doc, StringBuilder builder) {
        Elements tables = doc.getElementsByTag("table");
        if (!tables.isEmpty()) {
            builder.append("Examples:\n");
            Element table = tables.first();
            Elements headers = table.select("tr").first().select("th");
            for (Element header : headers) {
                builder.append("|").append(header.text());
            }
            builder.append("|\n");
            Elements data = table.select("tr");
            for (int i = 1; i < data.size(); i++) {
                for (Element cell : data.get(i).select("td")) {
                    builder.append("|").append(cell.text());
                }
                builder.append("|\n");
            }
        }
    }

    protected void cleanNodes(Element element, String tag) {
        Elements elementsByTag = element.getElementsByTag(tag);
        for (Element el : elementsByTag) {
            if (el == null || el.parent() == null) {
                continue;
            }
            Elements children = el.children().select(tag);
            for (Element child : children) {
                cleanNodes(child, tag);
            }
            TextNode text = new TextNode(el.text() + "<br/>", "");
            el.replaceWith(text);
        }
    }
}
