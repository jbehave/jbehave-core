package org.jbehave.examples.trader.i18n;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.ParameterConverters.NumberConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.core.steps.BeforeAfterSteps;

/**
 * Abstract base ConfigurableEmbedder allowing localization of multiple stories
 * via JUnit. The concrete extension need only specify:
 * <ul>
 * <li>language: e.g. "fr", "it, "pt"</li>
 * <li>story pattern to look up stories</li>
 * <li>language-specific steps instance</li>
 * </ul>
 */
public abstract class LocalizedStories extends JUnitStories {

    @Override
    public Configuration configuration() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());        
        Keywords keywords = new LocalizedKeywords(locale());
        Properties properties = new Properties();
        properties.setProperty("reports", "ftl/jbehave-reports.ftl");
        properties.setProperty("encoding", "UTF-8");
        LoadFromClasspath resourceLoader = new LoadFromClasspath(classLoader);
        TableParsers tableParsers = new TableParsers();
        TableTransformers tableTransformers = new TableTransformers();
        ParameterControls parameterControls = new ParameterControls();
        ParameterConverters parameterConverters = new ParameterConverters(resourceLoader, parameterControls,
                tableTransformers, true)
                .addConverters(customConverters(keywords, resourceLoader, tableParsers, tableTransformers));
        return new MostUsefulConfiguration()
                .useKeywords(keywords)
                .useStoryParser(new RegexStoryParser(keywords, resourceLoader, tableTransformers))
                .useStoryLoader(resourceLoader)
                .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(codeLocation)
                    .withPathResolver(new ResolveToSimpleName())
                    .withDefaultFormats()
                    .withFormats(CONSOLE, TXT, HTML, XML)
                    .withFailureTrace(false)
                    .withViewResources(properties)
                    .withKeywords(keywords))
                .useParameterConverters(parameterConverters)
                .useParameterControls(parameterControls)
                .useTableTransformers(tableTransformers);
    }
    
    private ParameterConverter[] customConverters(Keywords keywords, ResourceLoader resourceLoader,
                                                  TableParsers tableParsers, TableTransformers tableTransformers) {
        return new ParameterConverter[] { new NumberConverter(NumberFormat.getInstance(locale())),
                new ExamplesTableConverter(new ExamplesTableFactory(keywords, resourceLoader, tableParsers, tableTransformers)) };
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), localizedSteps(), new BeforeAfterSteps());
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), storyPattern(), "");
    }

    protected abstract Locale locale();
    
    protected abstract String storyPattern();
    
    protected abstract Object localizedSteps();
    
}
