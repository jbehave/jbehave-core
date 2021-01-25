package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.hamcrest.Matchers;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class NullStoryReporterBehaviour {

    private final String NL = System.getProperty("line.separator");

    @Test
    public void shouldOnlyReportOverriddenMethods() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);
        StoryReporter reporter = new NullStoryReporter(){

            @Override
            public void beforeStory(Story story, boolean givenStory) {
                printStream.println("beforeStory");
            }

            @Override
            public void afterStory(boolean givenStory) {
                printStream.println("afterStory");
            }

        };
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        assertThat(out.toString(), Matchers.equalTo("beforeStory" + NL + "afterStory" + NL));
    }

}
