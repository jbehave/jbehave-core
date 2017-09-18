package org.jbehave.core.configuration.needle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.needle.UsingNeedle;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.needle.NeedleStepsFactoryBehaviour.FooSteps;
import org.jbehave.core.steps.needle.NeedleStepsFactoryBehaviour.FooStepsWithDependency;
import org.jbehave.core.steps.needle.ValueGetter;
import org.junit.Assert;
import org.junit.Test;

public class NeedleAnnotationBuilderBehaviour {

    @Test
    public void shouldBuildConfigurationFromAnnotationsUsingConfigureAndGuiceConverters() {
        final AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedUsingConfigureAndNeedleConverters.class);
        final Configuration configuration = builderAnnotated
                .buildConfiguration();
        assertThatCustomObjectIsConverted(configuration.parameterConverters());
        assertThatDateIsConvertedWithFormat(
                configuration.parameterConverters(), new SimpleDateFormat(
                        "yyyy-MM-dd"));
        assertThatExamplesTableIsConverted(configuration.parameterConverters());
    }

    private void assertThatCustomObjectIsConverted(
            final ParameterConverters parameterConverters) {
        assertThat(((CustomObject) parameterConverters.convert("value",
                CustomObject.class)).toString(), equalTo(new CustomObject(
                "value").toString()));
    }

    private void assertThatDateIsConvertedWithFormat(
            final ParameterConverters parameterConverters,
            final DateFormat dateFormat) {
        final String date = "2010-10-10";
        try {
            assertThat((Date) parameterConverters.convert(date, Date.class),
                    equalTo(dateFormat.parse(date)));
        } catch (final ParseException e) {
            Assert.fail();
        }
    }

    private void assertThatExamplesTableIsConverted(
            final ParameterConverters parameterConverters) {
        final String tableAsString = "||one||two||\n" + "|1|2|";
        final ExamplesTable table = new ExamplesTable(tableAsString);
        assertThat(table.getHeaders(), hasItems("one", "two"));
    }

    @Test
    public void shouldBuildDefaultConfigurationIfAnnotationOrAnnotatedValuesNotPresent() {
        final AnnotationBuilder builderNotAnnotated = new NeedleAnnotationBuilder(
                NotAnnotated.class);
        assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(),
                new MostUsefulConfiguration());
        final AnnotationBuilder builderAnnotatedWithoutModules = new NeedleAnnotationBuilder(
                AnnotatedWithoutInjectors.class);
        assertThatConfigurationIs(
                builderAnnotatedWithoutModules.buildConfiguration(),
                new MostUsefulConfiguration());
    }

    private void assertThatConfigurationIs(
            final Configuration builtConfiguration,
            final Configuration defaultConfiguration) {
        assertThat(builtConfiguration.failureStrategy(),
                instanceOf(defaultConfiguration.failureStrategy().getClass()));
        assertThat(builtConfiguration.storyLoader(),
                instanceOf(defaultConfiguration.storyLoader().getClass()));
        assertThat(builtConfiguration.stepPatternParser(),
                instanceOf(defaultConfiguration.stepPatternParser().getClass()));
        assertThat(builtConfiguration.storyReporterBuilder().formats(),
                equalTo(defaultConfiguration.storyReporterBuilder().formats()));
        assertThat(builtConfiguration.storyReporterBuilder().outputDirectory(),
                equalTo(defaultConfiguration.storyReporterBuilder()
                        .outputDirectory()));
        assertThat(builtConfiguration.storyReporterBuilder().viewResources(),
                equalTo(defaultConfiguration.storyReporterBuilder()
                        .viewResources()));
        assertThat(builtConfiguration.storyReporterBuilder()
                .reportFailureTrace(), equalTo(defaultConfiguration
                .storyReporterBuilder().reportFailureTrace()));
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingNeedle() {
        final AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedUsingNeedle.class);
        final Configuration configuration = builderAnnotated
                .buildConfiguration();
        assertTrue(builderAnnotated.buildCandidateSteps(configuration)
                .isEmpty());
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndNeedle() {
        final AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedUsingStepsAndNeedle.class);
        final Configuration configuration = builderAnnotated
                .buildConfiguration();
        assertThatStepsInstancesAre(
                builderAnnotated.buildCandidateSteps(configuration),
                FooSteps.class);
    }

    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndGuiceAndConverters() {
        final AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedUsingConfigureAndNeedleConverters.class);
        final Configuration configuration = builderAnnotated
                .buildConfiguration();
        assertThatStepsInstancesAre(
                builderAnnotated.buildCandidateSteps(configuration),
                FooSteps.class);
    }

    @Test
    public void shouldBuildEmptyStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
        final AnnotationBuilder builderNotAnnotated = new NeedleAnnotationBuilder(
                NotAnnotated.class);
        assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());

        final AnnotationBuilder builderAnnotatedWithoutLocations = new NeedleAnnotationBuilder(
                AnnotatedWithoutInjectors.class);
        assertThatStepsInstancesAre(builderAnnotatedWithoutLocations
                .buildCandidateSteps());
    }

    @Test
    public void shouldBuildStepsList() {
        final AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedMultipleSteps.class);
        final List<CandidateSteps> actual = builderAnnotated
                .buildCandidateSteps();
        assertThatStepsInstancesAre(actual, FooStepsWithDependency.class,
                FooSteps.class);
    }

    @Test
    public void shouldCreateOnlyOneContainerForMultipleBuildInvocations() {
        final NeedleAnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedUsingStepsAndNeedle.class);
        builderAnnotated.buildConfiguration();
        assertTrue(!builderAnnotated.getProvider().isEmpty());
    }

    @Test
    public void shouldSupplyInjectors() {
        final NeedleAnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
                AnnotatedWithStepsWithDependency.class);
        final List<CandidateSteps> buildCandidateSteps = builderAnnotated
                .buildCandidateSteps();
        assertThatStepsInstancesAre(buildCandidateSteps,
                FooStepsWithDependency.class);
        final ValueGetter getter = ((FooStepsWithDependency) ((Steps) buildCandidateSteps
                .get(0)).instance()).getGetter();
        assertNotNull(getter);
        assertThat((String) getter.getValue(), is(ValueGetter.VALUE));
    }

    private void assertThatStepsInstancesAre(
            final List<CandidateSteps> candidateSteps,
            final Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));

        // transform candidateSteps to Set of classes
        final Set<Class<?>> candidateStepClasses = new HashSet<Class<?>>();
        for (final CandidateSteps step : candidateSteps) {
            candidateStepClasses.add(((Steps) step).instance().getClass());
        }
        assertThat(candidateStepClasses, hasItems(stepsClasses));
    }

    @Configure()
    @UsingNeedle(provider = { ValueGetterProvider.class })
    private static class AnnotatedUsingNeedle {

    }

    @Configure()
    @UsingSteps(instances = { FooSteps.class })
    @UsingNeedle(provider = { ValueGetterProvider.class })
    private static class AnnotatedUsingStepsAndNeedle {

    }

    @Configure(parameterConverters = { MyExampleTableConverter.class,
            MyDateConverter.class, CustomConverter.class })
    @UsingSteps(instances = { FooSteps.class })
    @UsingNeedle(provider = { ValueGetterProvider.class })
    private static class AnnotatedUsingConfigureAndNeedleConverters {

    }

    @Configure()
    @UsingNeedle()
    private static class AnnotatedWithoutInjectors {

    }

    @Configure()
    @UsingSteps(instances = { FooStepsWithDependency.class })
    @UsingNeedle()
    private static class AnnotatedWithStepsWithDependency {

    }

    @Configure()
    @UsingSteps(instances = { FooStepsWithDependency.class, FooSteps.class })
    @UsingNeedle()
    private static class AnnotatedMultipleSteps {

    }

    private static class NotAnnotated {

    }

    public static class CustomConverter implements ParameterConverter {

        public boolean accept(final Type type) {
            return ((Class<?>) type).isAssignableFrom(CustomObject.class);
        }

        public Object convertValue(final String value, final Type type) {
            return new CustomObject(value);
        }
    }

    public static class MyExampleTableConverter extends ParameterConverters.ExamplesTableConverter {

        public MyExampleTableConverter() {
            super(new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        }

    }

    public static class MyDateConverter extends
            ParameterConverters.DateConverter {

        public MyDateConverter() {
            super(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }

    public static class CustomObject {

        private final String value;

        public CustomObject(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
