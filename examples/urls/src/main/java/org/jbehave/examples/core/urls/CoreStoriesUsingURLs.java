package org.jbehave.examples.core.urls;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.examples.core.CoreStories;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * <p>
 * Example of how multiple stories can be run via JUnit.
 * </p>
 * <p>
 * Stories are specified as URLs and correspondingly the {@link LoadFromURL}
 * story loader is configured and the story paths are looked up as "file:" URLs.
 * It extends CoreStories simply for convenience, in order to avoid
 * duplicating common configuration.
 * </p>
 */
public class CoreStoriesUsingURLs extends CoreStories {

    public CoreStoriesUsingURLs() {
    }

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();        
        StoryReporterBuilder builder = configuration.storyReporterBuilder();
        builder.withPathResolver(new ResolveToSimpleName());
        return configuration.useStoryLoader(new LoadFromURL());
    }

    @Override
    protected List<String> storyPaths() {
        // Specify story paths as URLs
        String codeLocation = codeLocationFromPath("../core/src/main/java").getFile();
        return new StoryFinder().findPaths(codeLocation, asList("**/trader_is_alerted_of_status.story",
                "**/traders_can_be_searched.story"), asList(""), "file:" + codeLocation);
    }

}