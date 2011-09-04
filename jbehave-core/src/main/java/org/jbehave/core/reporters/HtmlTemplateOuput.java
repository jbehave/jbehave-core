package org.jbehave.core.reporters;

import java.io.File;

import org.jbehave.core.configuration.Keywords;

/**
 * A templateable output that generates HTML. By default it uses
 * {@link FreemarkerProcessor} and template path <b>ftl/jbehave-html-output.ftl</b>,
 * but custom processors based on other templating systems can be provided and/or 
 * other template paths can be configured.
 */
public class HtmlTemplateOuput extends TemplateableOutput {

    public HtmlTemplateOuput(File file, Keywords keywords) {
        super(file, keywords, new FreemarkerProcessor(), "ftl/jbehave-html-output.ftl");
    }

    public HtmlTemplateOuput(File file, Keywords keywords, TemplateProcessor processor, String templatePath) {
        super(file, keywords, processor, templatePath);
    }

}
