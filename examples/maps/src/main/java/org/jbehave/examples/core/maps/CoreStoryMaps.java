package org.jbehave.examples.core.maps;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStoryMaps;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * <p>
 * Example of how stories can be mapped via JUnit.
 * </p>
 */
public class CoreStoryMaps extends JUnitStoryMaps {
    
    public CoreStoryMaps() {
        configuredEmbedder().useMetaFilters(metaFilters());
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
            .useStoryLoader(new LoadFromClasspath(this.getClass()))
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass())));
    }

    @Override
    public List<String> metaFilters() {
        return asList("+author *", "theme *","-skip");
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }
}