package org.jbehave.core.reporters;

import java.io.File;

import org.jbehave.core.configuration.Keywords;

/**
 * A templateable output that generates XML. By default it uses
 * {@link FreemarkerProcessor} and template path <b>ftl/jbehave-xml-output.ftl</b>,
 * but custom processors based on other templating systems can be provided and/or 
 * other template paths can be configured.
 */
public class XmlTemplateOutput extends TemplateableOutput {

    public XmlTemplateOutput(File file, Keywords keywords) {
        super(file, keywords, new FreemarkerProcessor(), "ftl/jbehave-xml-output.ftl");
    }

    public XmlTemplateOutput(File file, Keywords keywords, TemplateProcessor processor, String templatePath) {
        super(file, keywords, processor, templatePath);
    }

}
