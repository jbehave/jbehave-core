package org.jbehave.core.reporters;

import java.util.Properties;

public abstract class Format {

    public static final Format CONSOLE = new ConsoleFormat(false);

    public static final Format CONSOLE_WITH_STACK_TRACES = new ConsoleFormat(true);

    public static final Format IDE_CONSOLE = new Format("IDE_CONSOLE") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
            return new IdeOnlyConsoleOutput(storyReporterBuilder.keywords()).doReportFailureTrace(storyReporterBuilder.reportFailureTrace());
        }
    };

    public static final Format TXT = new Format("TXT") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("txt"));
            return new TxtOutput(factory.createPrintStream(), storyReporterBuilder.keywords()).doReportFailureTrace(storyReporterBuilder.reportFailureTrace());
        }
    };

    public static final Format HTML = new HtmlFormat(false);

    public static final Format HTML_WITH_STACK_TRACES = new HtmlFormat(true);

    public static final Format XML = new Format("XML") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("xml"));
            return new XmlOutput(factory.createPrintStream(), storyReporterBuilder.keywords()).doReportFailureTrace(storyReporterBuilder.reportFailureTrace());
        }
    };

    public static final Format STATS = new Format("STATS") {
        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("stats"));
            return new PostStoryStatisticsCollector(factory.createPrintStream());
        }
    };

    private final String name;

    public Format(String name) {
        this.name = name;
    }

    public abstract StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder);

    public String name() {
        return name;
    }

    private static class ConsoleFormat extends Format {
        private final boolean withTraces;

        public ConsoleFormat(boolean withTraces) {
            super("CONSOLE");
            this.withTraces = withTraces;
        }

        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
            return new ConsoleOutput(new Properties(), storyReporterBuilder.keywords(), false).doReportFailureTrace(storyReporterBuilder.reportFailureTrace());
        }
    }

    private static class HtmlFormat extends Format {
        private boolean withTraces;

        public HtmlFormat(boolean withTraces) {
            super("HTML");
            this.withTraces = withTraces;
        }

        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
            factory.useConfiguration(storyReporterBuilder.fileConfiguration("html"));
            return new HtmlOutput(factory.createPrintStream(), new Properties(), storyReporterBuilder.keywords(), withTraces).doReportFailureTrace(storyReporterBuilder.reportFailureTrace());
        }
    }
}
