import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import org.jbehave.core.reporters.StoryReporterBuilder;

class MyReportBuilder extends StoryReporterBuilder {
    public MyReportBuilder() {
        this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats();
    }
}
