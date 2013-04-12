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
import java.util.List;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.needle.UsingNeedle;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.model.ExamplesTable;
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
		AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
				AnnotatedUsingConfigureAndNeedleConverters.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertThatCustomObjectIsConverted(configuration.parameterConverters());
		assertThatDateIsConvertedWithFormat(configuration.parameterConverters(), new SimpleDateFormat("yyyy-MM-dd"));
		assertThatExamplesTableIsConverted(configuration.parameterConverters());
	}

	private void assertThatCustomObjectIsConverted(ParameterConverters parameterConverters) {
		assertThat(((CustomObject) parameterConverters.convert("value", CustomObject.class)).toString(),
				equalTo(new CustomObject("value").toString()));
	}

	private void assertThatDateIsConvertedWithFormat(ParameterConverters parameterConverters, DateFormat dateFormat) {
		String date = "2010-10-10";
		try {
			assertThat((Date) parameterConverters.convert(date, Date.class), equalTo(dateFormat.parse(date)));
		} catch (ParseException e) {
			Assert.fail();
		}
	}

	private void assertThatExamplesTableIsConverted(ParameterConverters parameterConverters) {
		String tableAsString = "||one||two||\n" + "|1|2|";
		ExamplesTable table = new ExamplesTable(tableAsString);
		assertThat(table.getHeaders(), hasItems("one", "two"));
	}

	@Test
	public void shouldBuildDefaultConfigurationIfAnnotationOrAnnotatedValuesNotPresent() {
		AnnotationBuilder builderNotAnnotated = new NeedleAnnotationBuilder(NotAnnotated.class);
		assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(), new MostUsefulConfiguration());
		AnnotationBuilder builderAnnotatedWithoutModules = new NeedleAnnotationBuilder(AnnotatedWithoutInjectors.class);
		assertThatConfigurationIs(builderAnnotatedWithoutModules.buildConfiguration(), new MostUsefulConfiguration());
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
	public void shouldBuildCandidateStepsFromAnnotationsUsingNeedle() {
		AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(AnnotatedUsingNeedle.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertTrue(builderAnnotated.buildCandidateSteps(configuration).isEmpty());
	}

	@Test
	public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndNeedle() {
		AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(AnnotatedUsingStepsAndNeedle.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class);
	}

	@Test
	public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndGuiceAndConverters() {
		AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(
				AnnotatedUsingConfigureAndNeedleConverters.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), FooSteps.class);
	}

	@Test
	public void shouldBuildEmptyStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
		AnnotationBuilder builderNotAnnotated = new NeedleAnnotationBuilder(NotAnnotated.class);
		assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());

		AnnotationBuilder builderAnnotatedWithoutLocations = new NeedleAnnotationBuilder(
				AnnotatedWithoutInjectors.class);
		assertThatStepsInstancesAre(builderAnnotatedWithoutLocations.buildCandidateSteps());
	}

	@Test
	public void shouldBuildStepsList() {
		AnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(AnnotatedMultipleSteps.class);
		List<CandidateSteps> actual = builderAnnotated.buildCandidateSteps();
		assertThatStepsInstancesAre(actual, FooSteps.class, FooStepsWithDependency.class);
	}

	@Test
	public void shouldCreateOnlyOneContainerForMultipleBuildInvocations() {
		NeedleAnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(AnnotatedUsingStepsAndNeedle.class);
		builderAnnotated.buildConfiguration();
		assertTrue(!builderAnnotated.getProvider().isEmpty());
	}

	@Test
	public void shouldSupplyInjectors() {
		NeedleAnnotationBuilder builderAnnotated = new NeedleAnnotationBuilder(AnnotatedWithStepsWithDependency.class);
		List<CandidateSteps> buildCandidateSteps = builderAnnotated.buildCandidateSteps();
		assertThatStepsInstancesAre(buildCandidateSteps, FooStepsWithDependency.class);
		ValueGetter getter = ((FooStepsWithDependency) ((Steps) buildCandidateSteps.get(0)).instance()).getGetter();
		assertNotNull(getter);
		assertThat((String) getter.getValue(), is(ValueGetter.VALUE));
	}

	// TODO currently this method depends on order of elements in annotation
	// FIXME provide sorting of array
	private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
		assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
		for (int i = 0; i < stepsClasses.length; i++) {
			assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
		}
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

	@Configure(parameterConverters = { MyExampleTableConverter.class, MyDateConverter.class, CustomConverter.class })
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

		public boolean accept(Type type) {
			return ((Class<?>) type).isAssignableFrom(CustomObject.class);
		}

		public Object convertValue(String value, Type type) {
			return new CustomObject(value);
		}
	}

	public static class MyExampleTableConverter extends ParameterConverters.ExamplesTableConverter {

		public MyExampleTableConverter() {
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
