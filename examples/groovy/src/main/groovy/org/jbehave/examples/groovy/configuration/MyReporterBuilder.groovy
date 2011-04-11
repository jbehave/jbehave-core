import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import org.jbehave.core.reporters.StoryReporterBuilder;

class MyReportBuilder extends StoryReporterBuilder {
    public MyReportBuilder() {
        this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats();
    }
}
