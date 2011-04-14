package org.jbehave.core.io.odf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.w3c.dom.NodeList;

import static org.apache.commons.lang.StringUtils.join;

public class OdfUtils {

    public static OdfTextDocument loadOdt(InputStream resourceAsStream) {
        try {
            return (OdfTextDocument) OdfTextDocument.loadDocument(resourceAsStream);
        } catch (Exception cause) {
            throw new OdfDocumentLoadingFailed(resourceAsStream, cause);
        }
    }

    public static String parseOdt(OdfTextDocument document) {
        List<String> lines = new ArrayList<String>();
        try {
            XPath xpath = document.getContentDom().getXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//text:h|//text:p", document.getContentDom(), XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String textContent = nodeList.item(i).getTextContent();
                lines.add(textContent);
            }
        } catch (Exception e) {
            throw new OdfDocumentParsingFailed(document, e);
        }
        return join(lines, System.getProperty("line.separator"));
    }
    
    @SuppressWarnings("serial")
    public static class OdfDocumentLoadingFailed extends RuntimeException {

        public OdfDocumentLoadingFailed(InputStream stream, Throwable cause) {
            super("Failed to load ODF document from stream "+stream, cause);
        }
        
    }

    @SuppressWarnings("serial")
    public static class OdfDocumentParsingFailed extends RuntimeException {

        public OdfDocumentParsingFailed(OdfDocument document, Throwable cause) {
            super("Failed to parse ODF document "+document, cause);
        }
        
    }

}
