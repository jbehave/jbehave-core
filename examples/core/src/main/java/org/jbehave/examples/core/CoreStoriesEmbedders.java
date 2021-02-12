package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.*;

import java.text.SimpleDateFormat;
import java.util.List;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.*;

/**
 * Example of how to use one or more Embedders to embed the story running into
 * any running environment, using any running framework. In this example we are
 * running via JUnit two separate methods. It can be run into an IDE or
 * command-line.
 */
public class CoreStoriesEmbedders {

    @org.junit.Test
    public void mapStories() {
        Embedder embedder = new Embedder();
        embedder.useMetaFilters(asList("+author *", "+theme *", "-skip"));
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.mapStoriesAsPaths(storyPaths);
    }

    @org.junit.Test
    public void runClasspathLoadedStoriesAsJUnit() {
        // CoreEmbedder defines the configuration and steps factory
        Embedder embedder = new CoreEmbedder();
        embedder.embedderControls().doIgnoreFailureInStories(true);
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        embedder.runStoriesAsPaths(storyPaths);
    }

    /**
     * Specifies the Embedder for the core example, providing the
     * Configuration and the InjectableStepsFactory, using classpath story loading.
     */
    public static class CoreEmbedder extends Embedder {

        @Override
        public EmbedderControls embedderControls() {
            return new EmbedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(true);
        }

        @Override
        public Configuration configuration() {
            Class<? extends CoreEmbedder> embedderClass = this.getClass();
            TableTransformers tableTransformers = new TableTransformers();
            LoadFromClasspath resourceLoader = new LoadFromClasspath(embedderClass.getClassLoader());
            return new MostUsefulConfiguration()
                .useStoryLoader(resourceLoader)
                .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(CodeLocations.codeLocationFromClass(embedderClass))
                    .withDefaultFormats()
                    .withFormats(CONSOLE, TXT, HTML, XML))
                .useTableTransformers(tableTransformers)
                .useParameterConverters(new ParameterConverters(resourceLoader, tableTransformers)
                        .addConverters(new ParameterConverters.DateConverter(new SimpleDateFormat("yyyy-MM-dd")))) // use custom date pattern
                .useStepPatternParser(new RegexPrefixCapturingPatternParser(
                                "%")) // use '%' instead of '$' to identify parameters
                .useStepMonitor(new SilentStepMonitor());
        }

        @Override
        public InjectableStepsFactory stepsFactory() {
            MyContext context = new MyContext();
            return new InstanceStepsFactory(configuration(),
                    new AndSteps(), new BankAccountSteps(), new BeforeAfterSteps(),
                    new CalendarSteps(), new CompositeSteps(), new CompositeNestedSteps(), new ContextSteps(context), new StepsContextSteps(),
                    new TableMappingSteps(), new IgnoringSteps(), new JsonSteps(),
                    new MetaParametrisationSteps(), new NamedAnnotationsSteps(), new NamedParametersSteps(),
                    new ParameterDelimitersSteps(), new ParametrisationByDelimitedNameSteps(), new ParametrisedSteps(),
                    new PendingSteps(), new PriorityMatchingSteps(),
                    new RestartingSteps(), new SandpitSteps(), new SearchSteps(),
                    new TableSteps(), new TraderSteps(new TradingService()), new VerbatimSteps()
            );
        }

        public List<String> storyPaths() {
            String filter = System.getProperty("story.filter", "**/*.story");
            return findPaths(filter, "**/custom/*.story,**/failing/*.story,**/given/*.story,**/pending/*.story");
        }

        protected List<String> findPaths(String include, String exclude) {
            return new StoryFinder().findPaths(codeLocationFromClass(CoreStories.class), include, exclude);
        }

    }
}
