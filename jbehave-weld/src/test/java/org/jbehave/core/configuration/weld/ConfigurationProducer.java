package org.jbehave.core.configuration.weld;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jbehave.core.annotations.weld.WeldConfiguration;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;


@ApplicationScoped
public class ConfigurationProducer
{
    public ConfigurationProducer() {}
    
    @Produces @WeldConfiguration
    Configuration getConfiguration() {
        
        Properties viewResources = new Properties();
        viewResources.setProperty("index", "my-reports-index.ftl");
        viewResources.setProperty("decorateNonHtml", "true");

        TableTransformers tableTransformers = new TableTransformers();
        LoadFromURL resourceLoader = new LoadFromURL();
        return new MostUsefulConfiguration()
                    .useStoryControls(new StoryControls()
                            .doDryRun(true)
                            .doSkipScenariosAfterFailure(true))
                    .useFailureStrategy(new SilentlyAbsorbingFailure())
                    .useStoryLoader(resourceLoader)
                    .useStepPatternParser(new RegexPrefixCapturingPatternParser("MyPrefix"))
                    .useStoryReporterBuilder(new StoryReporterBuilder()
                            .withDefaultFormats()
                            .withFormats(CONSOLE, HTML, TXT, XML)
                            .withKeywords(new LocalizedKeywords(Locale.ITALIAN))
                            .withRelativeDirectory("my-output-directory")
                            .withViewResources(viewResources).withFailureTrace(true))
                    .useParameterConverters(new ParameterConverters(resourceLoader, tableTransformers)
                            .addConverters(new CustomConverter(),new MyDateConverter()))
                    .useTableTransformers(tableTransformers);
                    
    }
    
    public static class CustomConverter implements ParameterConverter {

        public boolean accept(Type type) {
            return ((Class<?>) type).isAssignableFrom(CustomObject.class);
        }

        public Object convertValue(String value, Type type) {
            return new CustomObject(value);
        }
    }
    
    public static class MyDateConverter extends ParameterConverters.DateConverter {

        public MyDateConverter() {
            super(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }
    
    public static class CustomObject {

        private final String value;

        public CustomObject(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
