package org.jbehave.core.reporters;

import java.io.File;

import org.jbehave.core.configuration.Keywords;

public class HtmlTemplateOuput extends TemplateableOutput {

    public HtmlTemplateOuput(File file, Keywords keywords, TemplateProcessor processor) {
        super(file, keywords, processor, "ftl/jbehave-html-output.ftl");
    }

    public HtmlTemplateOuput(File file, Keywords keywords, TemplateProcessor processor, String templatePath) {
        super(file, keywords, processor, templatePath);
    }

}
