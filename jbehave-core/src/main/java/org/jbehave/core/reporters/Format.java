package org.jbehave.core.reporters;

import java.io.PrintStream;

import org.jbehave.core.reporters.StoryReporterBuilder.ProvidedFormat;

/**
 * A Format is a {@link StoryReporter} factory, allowing named-based pre-defined
 * story reporters to be configured via the
 * {@link StoryReporterBuilder#withFormats(Format...)} method. Users wanting to
 * configure their custom defined story reporters, can do so via the
 * {@link StoryReporterBuilder#withReporters(StoryReporter...)} method, or use
 * the {@link ProvidedFormat} wrapper.
 */
public abstract class Format {

    public static final Format CONSOLE = new Format("CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            return configureFailureTraces(storyReporterBuilder, new ConsoleOutput(storyReporterBuilder.keywords()));
        }
    };

    public static final Format ANSI_CONSOLE = new Format("ANSI_CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            return configureFailureTraces(storyReporterBuilder,
                    new ANSIConsoleOutput(storyReporterBuilder.keywords()).withCodes(storyReporterBuilder.codes()));
        }
    };

    public static final Format IDE_CONSOLE = new Format("IDE_CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            return configureFailureTraces(storyReporterBuilder, new IdeOnlyConsoleOutput(storyReporterBuilder.keywords()));
        }
    };

    public static final Format TEAMCITY_CONSOLE = new Format("TEAMCITY_CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                                                 StoryReporterBuilder storyReporterBuilder) {
            return configureFailureTraces(storyReporterBuilder, new TeamCityConsoleOutput(storyReporterBuilder.keywords()));
        }
    };

    public static final Format TXT = new Format("TXT") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("txt"));
            return configureFailureTraces(storyReporterBuilder,
                    new TxtOutput(factory.createPrintStream(), storyReporterBuilder.keywords()));
        }
    };

    public static final Format HTML = new Format("HTML") {

        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("html"));
            return configureFailureTraces(storyReporterBuilder,
                    new HtmlOutput(factory.createPrintStream(), storyReporterBuilder.keywords()));
        }
    };

    public static final Format XML = new Format("XML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                                                 StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("xml"));
            return configureFailureTraces(storyReporterBuilder,
                    new XmlOutput(factory.createPrintStream(), storyReporterBuilder.keywords()));
        }
    };

    public static final Format JSON = new Format("JSON") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                                                 StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("json"));
            return configureFailureTraces(storyReporterBuilder,
                    new JsonOutput(factory.createPrintStream(), storyReporterBuilder.keywords()));
        }
    };

    public static final Format HTML_TEMPLATE = new Format("HTML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("html"));
            return new HtmlTemplateOutput(factory.getOutputFile(), storyReporterBuilder.keywords());
        }
    };

    public static final Format XML_TEMPLATE = new Format("XML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("xml"));
            return new XmlTemplateOutput(factory.getOutputFile(), storyReporterBuilder.keywords());
        }
    };

    public static final Format JSON_TEMPLATE = new Format("JSON") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                                                 StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("json"));
            return new JsonTemplateOutput(factory.getOutputFile(), storyReporterBuilder.keywords());
        }
    };

    /**
     * STATS is needed by the final reports.html summary page.
     */
    public static final Format STATS = new Format("STATS") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("stats"));
            return new PostStoryStatisticsCollector(factory.createPrintStream());
        }
    };

    private final String name;

    public Format(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public abstract StoryReporter createStoryReporter(FilePrintStreamFactory factory,
            StoryReporterBuilder storyReporterBuilder);

    public static void println(PrintStream writer, Object what) {
        writer.println(what);
    }

    @Override
    public String toString() {
        return name;
    }

    private static PrintStreamOutput configureFailureTraces(StoryReporterBuilder storyReporterBuilder,
                                                            PrintStreamOutput output) {
        return output
                .doReportFailureTrace(storyReporterBuilder.reportFailureTrace())
                .doCompressFailureTrace(storyReporterBuilder.compressFailureTrace());
    }
}
