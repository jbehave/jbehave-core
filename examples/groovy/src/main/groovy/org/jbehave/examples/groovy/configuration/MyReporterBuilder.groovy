import org.jbehave.core.reporters.StoryReporterBuilder

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.*

class MyReportBuilder extends StoryReporterBuilder {
    public MyReportBuilder() {
        this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats();
    }
}
