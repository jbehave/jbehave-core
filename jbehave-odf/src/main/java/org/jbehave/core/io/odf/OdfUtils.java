package org.jbehave.core.io.odf;

import static org.apache.commons.lang3.StringUtils.join;
import static org.odftoolkit.simple.common.TextExtractor.newOdfTextExtractor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OdfUtils {

    public static TextDocument loadOdt(InputStream resourceAsStream) {
        try {
            return TextDocument.loadDocument(resourceAsStream);
        } catch (Exception cause) {
            throw new OdfDocumentLoadingFailed(resourceAsStream, cause);
        }
    }

    public static String parseOdt(TextDocument document) {
        List<String> lines = new ArrayList<>();

        try {
            NodeList list = document.getContentRoot().getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node item = list.item(i);
                if (isTextNode(item)) {
                    lines.add(parseTextNode(item));
                } else if (isTableNode(item)) {
                    lines.addAll(parseTable(item));
                }
            }
        } catch (Exception e) {
            throw new OdfDocumentParsingFailed(document, e);
        }

        return join(lines, System.getProperty("line.separator"));
    }

    private static Collection<String> parseTable(Node item) {
        ArrayList<String> lines = new ArrayList<>();
        Table table = Table.getInstance((TableTableElement) item);
        for (Row row : table.getRowList()) {
            lines.add(parseTableRow(row));
        }
        return lines;
    }

    private static String parseTableRow(Row row) {
        String line = "|";
        for (int i = 0; i < row.getCellCount(); i++) {
            Cell cell = row.getCellByIndex(i);
            line += cell.getDisplayText() + "|";
        }
        return line;
    }

    private static boolean isTableNode(Node item) {
        return item instanceof TableTableElement;
    }

    private static String parseTextNode(Node item) {
        TextParagraphElementBase textItem = (TextParagraphElementBase) item;
        return newOdfTextExtractor(textItem).getText();
    }

    private static boolean isTextNode(Node item) {
        return item instanceof TextParagraphElementBase;
    }

    @SuppressWarnings("serial")
    public static class OdfDocumentLoadingFailed extends RuntimeException {

        public OdfDocumentLoadingFailed(InputStream stream, Throwable cause) {
            super("Failed to load ODF document from stream " + stream, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class OdfDocumentParsingFailed extends RuntimeException {

        public OdfDocumentParsingFailed(Document document, Throwable cause) {
            super("Failed to parse ODF document " + document, cause);
        }

    }

}
