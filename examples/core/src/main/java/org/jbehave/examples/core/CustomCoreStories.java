package org.jbehave.examples.core;

import java.util.Properties;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.reporters.FreemarkerViewGenerator;

public class CustomCoreStories extends CoreStories {

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        Properties viewResources = new Properties();
        viewResources.put("reports", "ftl/custom-reports.ftl");
        configuration.useViewGenerator(new FreemarkerViewGenerator(this.getClass()));
        return configuration.useStoryReporterBuilder(configuration.storyReporterBuilder()
                .withViewResources(viewResources)
                .withFormats(CustomHtmlOutput.FORMAT));
    }

}
