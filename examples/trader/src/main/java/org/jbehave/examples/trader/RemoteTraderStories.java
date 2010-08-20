package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromURL;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * <p>
 * Example of how multiple remote stories can be run via JUnit.
 * </p>
 * <p>
 * Stories are specified as remote URLs and correspondingly the {@link LoadFromURL}
 * story loader is configured and the story paths are looked up as remote URLs.
 * It extends TraderStories simply for convenience, in order to avoid
 * duplicating common configuration.
 * </p>
 */
public class RemoteTraderStories extends TraderStories {

    @Override
    public Configuration configuration() {
        return super.configuration()
               .useStoryLoader(new LoadFromURL())
               .useStoryReporterBuilder(
                       new StoryReporterBuilder()
                           .withCodeLocation(codeLocationFromURL("http://jbehave.org/reference/examples/stories/"))
                           .withDefaultFormats()
                           .withFormats(CONSOLE, TXT, HTML, XML));
    }

    @Override
    protected List<String> storyPaths() {
        // Specify story paths as remote URLs
        String codeLocation = codeLocationFromURL("http://jbehave.org/reference/examples/stories/")
                .toExternalForm();
        return asList(codeLocation + "and_step.story");
    }

}