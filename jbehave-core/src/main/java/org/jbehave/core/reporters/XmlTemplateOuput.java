package org.jbehave.core.reporters;

import java.io.File;

import org.jbehave.core.configuration.Keywords;

/**
 * @deprecated Use {@link XmlTemplateOutput}
 */
public class XmlTemplateOuput extends XmlTemplateOutput {
	
    public XmlTemplateOuput(File file, Keywords keywords) {
        super(file, keywords);
    }

    public XmlTemplateOuput(File file, Keywords keywords, TemplateProcessor processor, String templatePath) {
        super(file, keywords, processor, templatePath);
    }

}
