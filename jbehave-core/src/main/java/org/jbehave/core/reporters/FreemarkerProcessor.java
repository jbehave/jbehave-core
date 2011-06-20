package org.jbehave.core.reporters;

import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

public class FreemarkerProcessor implements TemplateProcessor {
    
    public void process(String resource, Map<String, Object> dataModel, Writer writer) {
        Configuration configuration = configuration();
        try {
            configuration.getTemplate(resource).process(dataModel, writer);
        } catch (Exception e) {
            throw new FreemarkerProcessingFailed(configuration, resource, dataModel, e);
        }
    }

    public Configuration configuration() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(FreemarkerProcessor.class, "/");
        configuration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        return configuration;
    }

    @SuppressWarnings("serial")
    public static class FreemarkerProcessingFailed extends RuntimeException {

        public FreemarkerProcessingFailed(Configuration configuration, String resource, Map<String, Object> dataModel, Exception cause) {
            super("Freemarker failed to process template " + resource + " using configuration "+configuration + " and data model "+dataModel, cause);
        }
        
    }

}
