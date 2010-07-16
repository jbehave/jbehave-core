package org.jbehave.core.configuration.spring;

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

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.spring.SpringStepsFactoryBehaviour.FooSteps;
import org.jbehave.core.steps.spring.SpringStepsFactoryBehaviour.FooStepsWithDependency;
import org.junit.Assert;
import org.junit.Test;

public class SpringAnnotationBuilderBehaviour {

    @Test
    public void shouldBuildConfigurationFromAnnotations() {
        SpringAnnotationBuilder builder = new SpringAnnotationBuilder(AnnotatedUsingSpring.class);
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
        SpringAnnotationBuilder builderNotAnnotated = new SpringAnnotationBuilder(NotAnnotated.class);
        assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(), new MostUsefulConfiguration());
        SpringAnnotationBuilder builderAnnotatedWithoutLocations = new SpringAnnotationBuilder(
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
    public void shouldBuildCandidateStepsFromAnnotationsUsingSpring() {
        SpringAnnotationBuilder builderAnnotated = new SpringAnnotationBuilder(AnnotatedUsingSpring.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class,
                FooStepsWithDependency.class);
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndSpring() {
        SpringAnnotationBuilder builderAnnotated = new SpringAnnotationBuilder(AnnotatedUsingStepsAndSpring.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class);
    }

    @Test
    public void shouldBuildEmptyStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
        SpringAnnotationBuilder builderNotAnnotated = new SpringAnnotationBuilder(NotAnnotated.class);
        assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());
        SpringAnnotationBuilder builderAnnotatedWithoutModules = new SpringAnnotationBuilder(
                AnnotatedWithoutModules.class);
        assertThatStepsInstancesAre(builderAnnotatedWithoutModules.buildCandidateSteps());
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }
    }

    @Configure()
    @UsingSpring(locations = { "org/jbehave/core/configuration/spring/configuration.xml",
            "org/jbehave/core/steps/spring/steps.xml", "org/jbehave/core/steps/spring/steps-with-dependency.xml" })
    private static class AnnotatedUsingSpring {

    }

    @Configure()
    @UsingSteps(instances = { FooSteps.class })
    @UsingSpring(locations = { "org/jbehave/core/configuration/spring/configuration.xml" })
    private static class AnnotatedUsingStepsAndSpring {

    }

    @Configure()
    @UsingSpring()
    private static class AnnotatedWithoutModules {

    }

    private static class NotAnnotated {

    }

}
