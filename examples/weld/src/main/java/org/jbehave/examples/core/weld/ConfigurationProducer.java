package org.jbehave.examples.core.weld;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import java.text.SimpleDateFormat;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jbehave.core.annotations.weld.WeldConfiguration;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;

@ApplicationScoped
public class ConfigurationProducer {
    
    public ConfigurationProducer() {}

    @Produces @WeldConfiguration
    public Configuration getConfiguration() {
        LoadFromClasspath resourceLoader = new LoadFromClasspath(getClass().getClassLoader());
        TableTransformers tableTransformers = new TableTransformers();
        return new MostUsefulConfiguration()
            .useStoryControls(new StoryControls()
                .doDryRun(false)
                .doSkipScenariosAfterFailure(false))
            .useStoryLoader(resourceLoader)
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withDefaultFormats()
                .withFormats(CONSOLE, HTML, TXT, XML))
            .useParameterConverters(new ParameterConverters(resourceLoader, tableTransformers)
                .addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd"))))
            .useTableTransformers(tableTransformers);
    }
}
