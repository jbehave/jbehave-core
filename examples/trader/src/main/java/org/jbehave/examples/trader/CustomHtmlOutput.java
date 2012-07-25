package org.jbehave.examples.trader;

import java.io.File;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.FreemarkerProcessor;
import org.jbehave.core.reporters.HtmlTemplateOutput;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;

public class CustomHtmlOutput extends HtmlTemplateOutput {

    public CustomHtmlOutput(File file, Keywords keywords) {
        super(file, keywords, new FreemarkerProcessor(CustomHtmlOutput.class), "ftl/custom-html-output.ftl");
    }

    public static final Format FORMAT = new Format("HTML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("html"));
            return new CustomHtmlOutput(factory.getOutputFile(), storyReporterBuilder.keywords());
        }
    };

}
