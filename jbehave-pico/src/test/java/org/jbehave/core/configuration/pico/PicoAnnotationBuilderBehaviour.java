package org.jbehave.core.configuration.pico;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.pico.UsingPico;
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
import org.jbehave.core.steps.pico.PicoStepsFactoryBehaviour.FooSteps;
import org.jbehave.core.steps.pico.PicoStepsFactoryBehaviour.FooStepsWithDependency;
import org.junit.Assert;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;

public class PicoAnnotationBuilderBehaviour {

    @Test
    public void shouldBuildConfigurationFromAnnotations() {
        PicoAnnotationBuilder builder = new PicoAnnotationBuilder(AnnotatedUsingPico.class);
        Configuration configuration = builder.buildConfiguration();
        assertThat(configuration.failureStrategy(), instanceOf(SilentlyAbsorbingFailure.class));
        assertThat(configuration.storyLoader(), instanceOf(LoadFromURL.class));
        assertThat(configuration.stepPatternParser(), instanceOf(RegexPrefixCapturingPatternParser.class));
        assertThat(((RegexPrefixCapturingPatternParser) configuration.stepPatternParser()).getPrefix(),
                equalTo("MyPrefix"));
        assertThatDateIsConvertedWithFormat(configuration.parameterConverters(), new SimpleDateFormat("yyyy-MM-dd"));
        assertThat(configuration.storyReporterBuilder().formats(), hasItems(CONSOLE, HTML, TXT, XML, STATS));
        Keywords keywords = configuration.storyReporterBuilder().keywords();
        assertThat(keywords, instanceOf(LocalizedKeywords.class));
        assertThat(((LocalizedKeywords) keywords).getLocale(), equalTo(Locale.ITALIAN));
        assertThat(configuration.storyReporterBuilder().outputDirectory().getName(), equalTo("my-output-directory"));
        assertThat(configuration.storyReporterBuilder().viewResources().getProperty("index"),
                equalTo("my-reports-index.ftl"));
        assertThat(configuration.storyReporterBuilder().viewResources().getProperty("decorateNonHtml"), equalTo("true"));
        assertThat(configuration.storyReporterBuilder().reportFailureTrace(), is(true));
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
        PicoAnnotationBuilder builderNotAnnotated = new PicoAnnotationBuilder(NotAnnotated.class);
        assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(), new MostUsefulConfiguration());
        PicoAnnotationBuilder builderAnnotatedWithoutLocations = new PicoAnnotationBuilder(
                AnnotatedWithoutModules.class);
        assertThatConfigurationIs(builderAnnotatedWithoutLocations.buildConfiguration(), new MostUsefulConfiguration());
    }

    private void assertThatConfigurationIs(Configuration builtConfiguration, Configuration defaultConfiguration) {
        assertThat(builtConfiguration.failureStrategy(), instanceOf(defaultConfiguration.failureStrategy().getClass()));
        assertThat(builtConfiguration.storyLoader(), instanceOf(defaultConfiguration.storyLoader().getClass()));
        assertThat(builtConfiguration.stepPatternParser(), instanceOf(defaultConfiguration.stepPatternParser()
                .getClass()));
        assertThat(builtConfiguration.storyReporterBuilder().formats(), equalTo(defaultConfiguration
                .storyReporterBuilder().formats()));
        assertThat(builtConfiguration.storyReporterBuilder().outputDirectory(), equalTo(defaultConfiguration
                .storyReporterBuilder().outputDirectory()));
        assertThat(builtConfiguration.storyReporterBuilder().viewResources(), equalTo(defaultConfiguration
                .storyReporterBuilder().viewResources()));
        assertThat(builtConfiguration.storyReporterBuilder().reportFailureTrace(), equalTo(defaultConfiguration
                .storyReporterBuilder().reportFailureTrace()));
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingPico() {
        PicoAnnotationBuilder builderAnnotated = new PicoAnnotationBuilder(AnnotatedUsingPico.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class,
                FooStepsWithDependency.class);
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndPico() {
        PicoAnnotationBuilder builderAnnotated = new PicoAnnotationBuilder(
                AnnotatedUsingStepsAndPico.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class );
    }

    @Test
    public void shouldBuildEmptyStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
        PicoAnnotationBuilder builderNotAnnotated = new PicoAnnotationBuilder(NotAnnotated.class);
        assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());
        PicoAnnotationBuilder builderAnnotatedWithoutModules = new PicoAnnotationBuilder(AnnotatedWithoutModules.class);
        assertThatStepsInstancesAre(builderAnnotatedWithoutModules.buildCandidateSteps());
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }
    }

    @Configure()
    @UsingPico(modules = { ConfigurationModule.class, StepsModule.class })
    private static class AnnotatedUsingPico {

    }

    @Configure()
    @UsingSteps(instances = { FooSteps.class })
    @UsingPico(modules = { ConfigurationModule.class })
    private static class AnnotatedUsingStepsAndPico {

    }

    @Configure()
    @UsingPico()
    private static class AnnotatedWithoutModules {

    }

    private static class NotAnnotated {

    }

    public static class ConfigurationModule implements PicoModule {

        public void configure(MutablePicoContainer container) {
            container.addComponent(FailureStrategy.class, SilentlyAbsorbingFailure.class);
            container.addComponent(StepPatternParser.class, new RegexPrefixCapturingPatternParser("MyPrefix"));
            container.addComponent(StoryLoader.class, new LoadFromURL());
            container.addComponent(ParameterConverter.class, new DateConverter(new SimpleDateFormat("yyyy-MM-dd")));
            Properties viewResources = new Properties();
            viewResources.setProperty("index", "my-reports-index.ftl");
            viewResources.setProperty("decorateNonHtml", "true");
            container.addComponent(new StoryReporterBuilder().withDefaultFormats().withFormats(CONSOLE, HTML, TXT, XML)
                    .withKeywords(new LocalizedKeywords(Locale.ITALIAN)).withRelativeDirectory("my-output-directory")
                    .withViewResources(viewResources).withFailureTrace(true));
        }

    }

    public static class StepsModule implements PicoModule {

        public void configure(MutablePicoContainer container) {
            container.addComponent(FooSteps.class);
            container.addComponent(Integer.class, 42);
            container.addComponent(FooStepsWithDependency.class);
        }

    }

}
