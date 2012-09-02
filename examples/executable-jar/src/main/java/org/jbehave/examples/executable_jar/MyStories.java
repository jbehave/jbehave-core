package org.jbehave.examples.executable_jar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.examples.executable_jar.steps.MySteps;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

/**
 * <p>
 * {@link Embeddable} class to run multiple textual stories via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the {@link LoadFromClasspath} story loader is configured.
 * </p> 
 */
public class MyStories extends JUnitStories {

    public MyStories() {
        configuredEmbedder().embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(true)
        .doIgnoreFailureInView(true).useThreads(2).useStoryTimeoutInSecs(60);
    }

    @Override
    public Configuration configuration() {
        Class<? extends Embeddable> embeddableClass = this.getClass();
        // Start from default ParameterConverters instance
        ParameterConverters parameterConverters = new ParameterConverters();
        // factory to allow parameter conversion and loading from external resources (used by StoryParser too)
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(), new LoadFromClasspath(embeddableClass), parameterConverters);
        // add custom converters
        parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                new ExamplesTableConverter(examplesTableFactory));
        return new MostUsefulConfiguration()
        .useStoryLoader(new LoadFromClasspath(embeddableClass))
        .useStoryParser(new RegexStoryParser(examplesTableFactory)) 
        .useStoryReporterBuilder(new StoryReporterBuilder()
        .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
        .withDefaultFormats()
        .withFormats(CONSOLE, TXT, HTML, XML))
        .useParameterConverters(parameterConverters);
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new MySteps());
    }

    /*
     * since the files inside a jar in the classpath currently cannot be searched, you
     * have to list the paths for the stories directly.
     */

    @Override
    protected List<String> storyPaths() {
        return Arrays.asList("org/jbehave/examples/executable_jar/stories/jar.story",
                "org/jbehave/examples/executable_jar/stories/jar2.story");
    }

    public static void main(String[] args) throws Throwable {
        new MyStories().run();
    }
}
