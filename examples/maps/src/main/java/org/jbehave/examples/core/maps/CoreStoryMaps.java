package org.jbehave.examples.core.maps;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStoryMaps;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

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
        TableTransformers tableTransformers = new TableTransformers();
        ExamplesTableFactory tableFactory = new ExamplesTableFactory(new LoadFromClasspath(this.getClass()),
                tableTransformers);
        return new MostUsefulConfiguration()
            .useStoryParser(new RegexStoryParser(tableFactory))
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass())))
            .useTableTransformers(tableTransformers);
    }

    @Override
    protected List<String> metaFilters() {
        return asList("+author *", "theme *","-skip");
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }
}