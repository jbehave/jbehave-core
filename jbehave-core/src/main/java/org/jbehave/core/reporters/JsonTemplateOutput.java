package org.jbehave.core.reporters;

import org.jbehave.core.configuration.Keywords;

import java.io.File;

/**
 * A templateable output that generates JSON. By default it uses
 * {@link FreemarkerProcessor} and template path <b>ftl/jbehave-json-output.ftl</b>,
 * but custom processors based on other templating systems can be provided and/or 
 * other template paths can be configured.
 */
public class JsonTemplateOutput extends TemplateableOutput {

    public JsonTemplateOutput(File file, Keywords keywords) {
        super(file, keywords, new FreemarkerProcessor(), "ftl/jbehave-json-output.ftl");
    }

    public JsonTemplateOutput(File file, Keywords keywords, TemplateProcessor processor, String templatePath) {
        super(file, keywords, processor, templatePath);
    }

}
