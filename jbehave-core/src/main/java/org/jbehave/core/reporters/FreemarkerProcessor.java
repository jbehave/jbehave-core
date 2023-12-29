package org.jbehave.core.reporters;

import java.io.Writer;
import java.util.Map;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Version;

public class FreemarkerProcessor implements TemplateProcessor {
    private ClassLoader templateLoadingFrom;
        
    public FreemarkerProcessor() {
        this(FreemarkerProcessor.class);
    }

    public FreemarkerProcessor(Class<?> templateLoadingFrom) {
        this(templateLoadingFrom.getClassLoader());
    }

    public FreemarkerProcessor(ClassLoader templateLoadingFrom) {
        this.templateLoadingFrom = templateLoadingFrom;
    }

    @Override
    public void process(String resource, Map<String, Object> dataModel, Writer writer) {
        Configuration configuration = configuration();
        try {
            configuration.getTemplate(resource).process(dataModel, writer);
        } catch (Exception e) {
            throw new FreemarkerProcessingFailed(configuration, resource, dataModel, e);
        }
    }

    public Configuration configuration() {
        Version incompatibleImprovementsVersion = Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
        Configuration configuration = new Configuration(incompatibleImprovementsVersion);
        configuration.setClassLoaderForTemplateLoading(templateLoadingFrom, "/");
        configuration.setObjectWrapper(new BeansWrapperBuilder(incompatibleImprovementsVersion).build());
        return configuration;
    }

    @SuppressWarnings("serial")
    public static class FreemarkerProcessingFailed extends RuntimeException {

        public FreemarkerProcessingFailed(Configuration configuration, String resource, Map<String, Object> dataModel,
                Exception cause) {
            super("Freemarker failed to process template " + resource + " using configuration " + configuration
                    + " and data model " + dataModel, cause);
        }
        
    }

}
