package org.jbehave.examples.core;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.ANSI_CONSOLE;
import static org.jbehave.core.reporters.Format.HTML_TEMPLATE;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML_TEMPLATE;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.context.Context;
import org.jbehave.core.embedder.PropertyBasedEmbedderControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.reporters.ContextOutput;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.SurefireReporter;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BankAccountSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.CompositeNestedSteps;
import org.jbehave.examples.core.steps.CompositeSteps;
import org.jbehave.examples.core.steps.ContextSteps;
import org.jbehave.examples.core.steps.IgnoringSteps;
import org.jbehave.examples.core.steps.JsonSteps;
import org.jbehave.examples.core.steps.MetaParametrisationSteps;
import org.jbehave.examples.core.steps.MyContext;
import org.jbehave.examples.core.steps.NamedAnnotationsSteps;
import org.jbehave.examples.core.steps.NamedParametersSteps;
import org.jbehave.examples.core.steps.ParameterDelimitersSteps;
import org.jbehave.examples.core.steps.ParametrisationByDelimitedNameSteps;
import org.jbehave.examples.core.steps.ParametrisedSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.RestartingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.StepsContextSteps;
import org.jbehave.examples.core.steps.TableMappingSteps;
import org.jbehave.examples.core.steps.TableSteps;
import org.jbehave.examples.core.steps.TraderSteps;
import org.jbehave.examples.core.steps.VerbatimSteps;
import org.junit.runner.RunWith;

/**
 * <p>
 * Example of how multiple stories can be run via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the
 * {@link LoadFromClasspath} story loader is configured.
 * </p>
 */
@RunWith(JUnit4StoryRunner.class)
public class CoreStories extends JUnitStories {

    private Context context = new Context();
    private Format contextFormat = new ContextOutput(context);

    public CoreStories() {
        configuredEmbedder().embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(false)
                .doIgnoreFailureInView(true).doVerboseFailures(true).useThreads(2).useStoryTimeouts("60");
        configuredEmbedder().useEmbedderControls(new PropertyBasedEmbedderControls());
    }

    @Override
    public Configuration configuration() {
        // avoid re-instantiating configuration for the steps factory
        // alternative use #useConfiguration() in the constructor
        if (super.hasConfiguration()) {
            return super.configuration();
        }
        Class<? extends Embeddable> embeddableClass = this.getClass();
        Properties viewResources = new Properties();
        viewResources.put("decorateNonHtml", "true");
        viewResources.put("reports", "ftl/jbehave-reports.ftl");
        LocalizedKeywords keywords = new LocalizedKeywords();
        LoadFromClasspath resourceLoader = new LoadFromClasspath(embeddableClass);
        TableTransformers tableTransformers = new TableTransformers();
        ParameterControls parameterControls = new ParameterControls();
        // Start from default ParameterConverters instance
        ParameterConverters parameterConverters = new ParameterConverters(resourceLoader, tableTransformers);
        TableParsers tableParsers = new TableParsers(keywords, parameterConverters);
        // factory to allow parameter conversion and loading from external
        // resources (used by StoryParser too)
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(keywords, resourceLoader,
                parameterConverters, parameterControls, tableParsers, tableTransformers);
        // add custom converters
        parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                new ExamplesTableConverter(examplesTableFactory));
        SurefireReporter.Options options = new SurefireReporter.Options().useReportName("surefire")
                .withNamingStrategy(new SurefireReporter.BreadcrumbNamingStrategy()).doReportByStory(true);
        SurefireReporter surefireReporter = new SurefireReporter(embeddableClass, options);
        return new MostUsefulConfiguration()
                .useKeywords(keywords)
                .useStoryLoader(resourceLoader)
                .useParameterControls(parameterControls)
                .useExamplesTableFactory(examplesTableFactory)
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                .withCodeLocation(codeLocationFromClass(embeddableClass))
                                .withDefaultFormats().withViewResources(viewResources)
                                .withFormats(contextFormat, ANSI_CONSOLE, TXT, HTML_TEMPLATE, XML_TEMPLATE)
                                .withFailureTrace(true)
                                .withFailureTraceCompression(true)
                                .withSurefireReporter(surefireReporter))
                .useParameterConverters(parameterConverters)
                .useParameterControls(parameterControls)
                .useTableTransformers(tableTransformers)
                .useCompositePaths(new HashSet<>(findPaths("**/*.steps", null)));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        MyContext context = new MyContext();
        return new InstanceStepsFactory(configuration(), new AndSteps(), new BankAccountSteps(), new BeforeAfterSteps(),
                new CalendarSteps(), new CompositeSteps(), new CompositeNestedSteps(), new ContextSteps(context),
                new StepsContextSteps(), new TableMappingSteps(), new IgnoringSteps(), new JsonSteps(),
                new MetaParametrisationSteps(), new NamedAnnotationsSteps(), new NamedParametersSteps(),
                new ParameterDelimitersSteps(), new ParametrisationByDelimitedNameSteps(), new ParametrisedSteps(),
                new PendingSteps(), new PriorityMatchingSteps(), new RestartingSteps(), new SandpitSteps(),
                new SearchSteps(), new TableSteps(), new TraderSteps(new TradingService()), new VerbatimSteps()
        );
    }

    @Override
    public List<String> storyPaths() {
        String filter = System.getProperty("story.filter", "**/*.story");
        return findPaths(filter, "**/custom/*.story,**/failing/*.story,**/given/*.story,**/pending/*.story");
    }

    protected List<String> findPaths(String include, String exclude) {
        return new StoryFinder().findPaths(codeLocationFromClass(CoreStories.class), include, exclude);
    }
}
