package org.jbehave.core.configuration.guice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.STATS;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.guice.GuiceStepsFactoryBehaviour.FooSteps;
import org.jbehave.core.steps.guice.GuiceStepsFactoryBehaviour.FooStepsWithDependency;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

public class GuiceAnnotationBuilderBehaviour {

    @Test
    public void shouldBuildConfigurationFromAnnotations() {
        AnnotationBuilder builder = new GuiceAnnotationBuilder(AnnotatedUsingGuice.class);
        Configuration configuration = builder.buildConfiguration();
        assertThat(configuration.failureStrategy(), instanceOf(SilentlyAbsorbingFailure.class));
        assertThat(configuration.storyLoader(), instanceOf(LoadFromURL.class));
        assertThat(configuration.stepPatternParser(), instanceOf(RegexPrefixCapturingPatternParser.class));
        assertThat(((RegexPrefixCapturingPatternParser)configuration.stepPatternParser()).getPrefix(), equalTo("MyPrefix"));
        assertThatCustomObjectIsConverted(configuration.parameterConverters());
        assertThatDateIsConvertedWithFormat(configuration.parameterConverters(), new SimpleDateFormat("yyyy-MM-dd"));
        assertThat(configuration.storyReporterBuilder().formats(), hasItems(CONSOLE, HTML, TXT, XML, STATS));
        Keywords keywords = configuration.storyReporterBuilder().keywords();
        assertThat(keywords, instanceOf(LocalizedKeywords.class));        
        assertThat(((LocalizedKeywords)keywords).getLocale(), equalTo(Locale.ITALIAN));        
        assertThat(configuration.storyReporterBuilder().outputDirectory().getName(), equalTo("my-output-directory"));
        assertThat(configuration.storyReporterBuilder().viewResources().getProperty("index"), equalTo("my-reports-index.ftl"));
        assertThat(configuration.storyReporterBuilder().viewResources().getProperty("decorateNonHtml"), equalTo("true"));
        assertThat(configuration.storyReporterBuilder().reportFailureTrace(), is(true));        
    }

    private void assertThatCustomObjectIsConverted(ParameterConverters parameterConverters) {
        assertThat(((CustomObject)parameterConverters.convert("value", CustomObject.class)).toString(), equalTo(new CustomObject("value").toString()));
    }

    private void assertThatDateIsConvertedWithFormat(ParameterConverters parameterConverters, DateFormat dateFormat) {
        String date = "2010-10-10";
        try {
            assertThat((Date) parameterConverters.convert(date, Date.class), equalTo(dateFormat.parse(date)));
        } catch (ParseException e) {
            Assert.fail();
        }
    }

    @Test
    public void shouldBuildDefaultConfigurationIfAnnotationOrAnnotatedValuesNotPresent() {
        AnnotationBuilder builderNotAnnotated = new GuiceAnnotationBuilder(NotAnnotated.class);
        assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(), new MostUsefulConfiguration());
        AnnotationBuilder builderAnnotatedWithoutModules = new GuiceAnnotationBuilder(AnnotatedWithoutModules.class);
        assertThatConfigurationIs(builderAnnotatedWithoutModules.buildConfiguration(), new MostUsefulConfiguration());
    }

    private void assertThatConfigurationIs(Configuration builtConfiguration,
            Configuration defaultConfiguration) {
        assertThat(builtConfiguration.failureStrategy(), instanceOf(defaultConfiguration.failureStrategy().getClass()));
        assertThat(builtConfiguration.storyLoader(), instanceOf(defaultConfiguration.storyLoader().getClass()));
        assertThat(builtConfiguration.stepPatternParser(), instanceOf(defaultConfiguration.stepPatternParser().getClass()));
        assertThat(builtConfiguration.storyReporterBuilder().formats(), equalTo(defaultConfiguration.storyReporterBuilder().formats()));
        assertThat(builtConfiguration.storyReporterBuilder().outputDirectory(), equalTo(defaultConfiguration.storyReporterBuilder().outputDirectory()));
        assertThat(builtConfiguration.storyReporterBuilder().viewResources(), equalTo(defaultConfiguration.storyReporterBuilder().viewResources()));
        assertThat(builtConfiguration.storyReporterBuilder().reportFailureTrace(), equalTo(defaultConfiguration.storyReporterBuilder().reportFailureTrace()));
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingGuice() {    	
        AnnotationBuilder builderAnnotated = new GuiceAnnotationBuilder(AnnotatedUsingGuice.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class,
                FooStepsWithDependency.class);
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndGuice() {        
        AnnotationBuilder builderAnnotated = new GuiceAnnotationBuilder(AnnotatedUsingStepsAndGuice.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class);
    }

    @Test
    public void shouldBuildEmptyStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
        AnnotationBuilder builderNotAnnotated = new GuiceAnnotationBuilder(NotAnnotated.class);
        assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());
        AnnotationBuilder builderAnnotatedWithoutLocations = new GuiceAnnotationBuilder(AnnotatedWithoutModules.class);
        assertThatStepsInstancesAre(builderAnnotatedWithoutLocations.buildCandidateSteps());
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }
    }

    @Configure()
    @UsingGuice(modules = { ConfigurationModule.class, StepsModule.class })
    private static class AnnotatedUsingGuice {

    }

    @Configure()
    @UsingSteps(instances = {FooSteps.class})
    @UsingGuice(modules = { ConfigurationModule.class})
    private static class AnnotatedUsingStepsAndGuice {

    }

    @Configure()
    @UsingGuice()
    private static class AnnotatedWithoutModules {

    }

    private static class NotAnnotated {

    }
    
    public static class ConfigurationModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(FailureStrategy.class).to(SilentlyAbsorbingFailure.class);
            bind(StepPatternParser.class).toInstance(new RegexPrefixCapturingPatternParser("MyPrefix"));
            bind(StoryLoader.class).toInstance(new LoadFromURL());
            Properties viewResources = new Properties();
            viewResources.setProperty("index", "my-reports-index.ftl");
            viewResources.setProperty("decorateNonHtml", "true");
            bind(StoryReporterBuilder.class).toInstance(new StoryReporterBuilder()
                .withDefaultFormats().withFormats(CONSOLE, HTML, TXT, XML)
                .withKeywords(new LocalizedKeywords(Locale.ITALIAN))
                .withOutputDirectory("my-output-directory")
                .withViewResources(viewResources)                
                .withFailureTrace(true)
            );
            Multibinder<ParameterConverter> multiBinder = Multibinder.newSetBinder(binder(), ParameterConverter.class);
            multiBinder.addBinding().toInstance(new CustomConverter());
            multiBinder.addBinding().toInstance(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")));
        }

    }

    public static class CustomConverter implements ParameterConverter {

        public boolean accept(Type type) {
            return ((Class<?>)type).isAssignableFrom(CustomObject.class);
        }

        public Object convertValue(String value, Type type) {
            return new CustomObject(value);
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

    public static class StepsModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(FooSteps.class).in(Scopes.SINGLETON);
            bind(Integer.class).toInstance(42);
            bind(FooStepsWithDependency.class).in(Scopes.SINGLETON);
        }

    }

}
