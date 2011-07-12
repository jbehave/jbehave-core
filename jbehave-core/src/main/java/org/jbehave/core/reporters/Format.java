package org.jbehave.core.reporters;


import java.io.PrintStream;

public abstract class Format {

    public static final Format CONSOLE = new Format("CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            return new ConsoleOutput(storyReporterBuilder.keywords()).doReportFailureTrace(
                    storyReporterBuilder.reportFailureTrace()).doCompressFailureTrace(
                    storyReporterBuilder.compressFailureTrace());
        }
    };

    public static final Format ANSI_CONSOLE = new Format("ANSI_CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                                                 StoryReporterBuilder storyReporterBuilder) {
            return new ANSIConsoleOutput(storyReporterBuilder.keywords())
                    .doReportFailureTrace(storyReporterBuilder.reportFailureTrace())
                    .doCompressFailureTrace(storyReporterBuilder.compressFailureTrace());
        }
    };

    public static final Format IDE_CONSOLE = new Format("IDE_CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            return new IdeOnlyConsoleOutput(storyReporterBuilder.keywords()).doReportFailureTrace(
                    storyReporterBuilder.reportFailureTrace()).doCompressFailureTrace(
                    storyReporterBuilder.compressFailureTrace());
        }
    };

    public static final Format TXT = new Format("TXT") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("txt"));
            return new TxtOutput(factory.createPrintStream(), storyReporterBuilder.keywords()).doReportFailureTrace(
                    storyReporterBuilder.reportFailureTrace()).doCompressFailureTrace(
                    storyReporterBuilder.compressFailureTrace());
        }
    };

    public static final Format HTML = new Format("HTML") {

        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("html"));
            return new HtmlOutput(factory.createPrintStream(), storyReporterBuilder.keywords()).doReportFailureTrace(
                    storyReporterBuilder.reportFailureTrace()).doCompressFailureTrace(
                    storyReporterBuilder.compressFailureTrace());
        }
    };

    public static final Format HTML_TEMPLATE = new Format("HTML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("html"));
            return new HtmlTemplateOuput(factory.getOutputFile(), storyReporterBuilder.keywords(), new FreemarkerProcessor());
        }
    };

    public static final Format XML = new Format("XML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("xml"));
            return new XmlOutput(factory.createPrintStream(), storyReporterBuilder.keywords()).doReportFailureTrace(
                    storyReporterBuilder.reportFailureTrace()).doCompressFailureTrace(
                    storyReporterBuilder.compressFailureTrace());
        }
    };

    /**
     * STATS is not just about output formats, it is needed by the final
     * reports.html summary page.
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

    public abstract StoryReporter createStoryReporter(FilePrintStreamFactory factory,
            StoryReporterBuilder storyReporterBuilder);

    public String name() {
        return name;
    }

    public static void println(PrintStream writer, Object what) {
        writer.println(what);
    }

}
