package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStoryMaps;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * <p>
 * Example of how stories can be mapped via JUnit.
 * </p>
 */
public class TraderStoryMaps extends JUnitStoryMaps {
    
    public TraderStoryMaps() {
        configuredEmbedder().useMetaFilters(metaFilters());
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass())));
    }

    @Override
    protected List<String> metaFilters() {
        return asList("+author *", "theme *","-skip");
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
                
    }
        
}