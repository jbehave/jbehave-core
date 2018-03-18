package org.jbehave.core.configuration.spring;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.*;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.spring.SpringStepsFactoryBehaviour.FooSteps;
import org.jbehave.core.steps.spring.SpringStepsFactoryBehaviour.FooStepsWithDependency;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.reporters.Format.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SpringAnnotationBuilderBehaviour {

	@Test
	public void shouldBuildConfigurationFromAnnotations() {
		SpringAnnotationBuilder builder = new SpringAnnotationBuilder(
				AnnotatedUsingSpring.class);
		Configuration configuration = builder.buildConfiguration();
		assertThat(configuration.storyControls().dryRun(), is(true));
		assertThat(configuration.storyControls().skipScenariosAfterFailure(),
				is(true));
		assertThat(configuration.failureStrategy(),
				instanceOf(SilentlyAbsorbingFailure.class));
		assertThat(configuration.storyLoader(), instanceOf(LoadFromURL.class));
		assertThat(configuration.stepPatternParser(),
				instanceOf(RegexPrefixCapturingPatternParser.class));
		assertThat(
				((RegexPrefixCapturingPatternParser) configuration.stepPatternParser())
						.getPrefix(), equalTo("MyPrefix"));
		assertThatDateIsConvertedWithFormat(
				configuration.parameterConverters(), new SimpleDateFormat(
						"yyyy-MM-dd"));
		assertThat(configuration.parameterControls().nameDelimiterLeft(),
				equalTo("["));
		assertThat(configuration.parameterControls().nameDelimiterRight(),
				equalTo("]"));
		assertThat(configuration.storyReporterBuilder().formats(),
				hasItems(CONSOLE, HTML, TXT, XML, STATS));
		Keywords keywords = configuration.storyReporterBuilder().keywords();
		assertThat(keywords, instanceOf(LocalizedKeywords.class));
		assertThat(((LocalizedKeywords) keywords).getLocale(),
				equalTo(Locale.ITALIAN));
		assertThat(configuration.storyReporterBuilder().outputDirectory()
				.getName(), equalTo("my-output-directory"));
		assertThat(configuration.storyReporterBuilder().viewResources()
				.getProperty("index"), equalTo("my-reports-index.ftl"));
		assertThat(configuration.storyReporterBuilder().viewResources()
				.getProperty("decorateNonHtml"), equalTo("true"));
		assertThat(configuration.storyReporterBuilder().reportFailureTrace(),
				is(true));
	}

	private void assertThatDateIsConvertedWithFormat(
			ParameterConverters parameterConverters, DateFormat dateFormat) {
		String date = "2010-10-10";
		try {
			assertThat((Date) parameterConverters.convert(date, Date.class),
					equalTo(dateFormat.parse(date)));
		} catch (ParseException e) {
			throw new AssertionError();
		}
	}

	@Test
	public void shouldBuildDefaultConfigurationIfAnnotationOrAnnotatedValuesNotPresent() {
		SpringAnnotationBuilder builderNotAnnotated = new SpringAnnotationBuilder(
				NotAnnotated.class);
		assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(),
				new MostUsefulConfiguration());
		SpringAnnotationBuilder builderAnnotatedWithoutLocations = new SpringAnnotationBuilder(
				AnnotatedWithoutResources.class);
		assertThatConfigurationIs(
				builderAnnotatedWithoutLocations.buildConfiguration(),
				new MostUsefulConfiguration());
	}

	private void assertThatConfigurationIs(Configuration builtConfiguration,
			Configuration defaultConfiguration) {
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
	public void shouldBuildCandidateStepsFromAnnotationsUsingSpring() {
		SpringAnnotationBuilder builderAnnotated = new SpringAnnotationBuilder(
				AnnotatedUsingSpring.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertThatStepsInstancesAreFound(
				builderAnnotated.buildCandidateSteps(configuration),
				FooSteps.class, FooStepsWithDependency.class);
	}

	@Test
	public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndSpring() {
		SpringAnnotationBuilder builderAnnotated = new SpringAnnotationBuilder(
				AnnotatedUsingStepsAndSpring.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertThatStepsInstancesAre(
				builderAnnotated.buildCandidateSteps(configuration),
				FooSteps.class);
	}

	@Test
	public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndInheritingPicoFromParent() {
		AnnotationBuilder builderAnnotated = new SpringAnnotationBuilder(
				InheritingAnnotatedUsingSteps.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
		assertThatStepsInstancesAre(
				builderAnnotated.buildCandidateSteps(configuration),
				FooSteps.class);
	}

	@Test
	public void shouldBuildEmptyStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
		SpringAnnotationBuilder builderNotAnnotated = new SpringAnnotationBuilder(
				NotAnnotated.class);
		assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());
		SpringAnnotationBuilder builderAnnotatedWithoutResources = new SpringAnnotationBuilder(
				AnnotatedWithoutResources.class);
		assertThatStepsInstancesAre(builderAnnotatedWithoutResources
				.buildCandidateSteps());
	}

	private void assertThatStepsInstancesAre(
			List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
		assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
		for (int i = 0; i < stepsClasses.length; i++) {
			assertThat(((Steps) candidateSteps.get(i)).instance(),
					instanceOf(stepsClasses[i]));
		}
	}

	private void assertThatStepsInstancesAreFound(
			List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
		boolean found = false;
		for (Class<?> stepsClass : stepsClasses) {
			for (CandidateSteps candidates : candidateSteps) {
				if (((Steps) candidates).instance().getClass()
						.equals(stepsClass)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new AssertionError("Class " + stepsClass + " not found in "
						+ candidateSteps);
			}
		}
	}

	@Test
	public void shouldNotBuildContainerIfResourceNotLoadable() {
		AnnotationMonitor annotationMonitor = mock(AnnotationMonitor.class);
		AnnotationBuilder builderUnloadableResource = new SpringAnnotationBuilder(
				AnnotatedWithUnloadableResource.class, annotationMonitor);
		try {
			assertThatStepsInstancesAre(builderUnloadableResource
					.buildCandidateSteps());
			throw new AssertionError("Exception expected");
		} catch (AnnotationBuilder.InstantiationFailed e) {
			// expected
		}
		verify(annotationMonitor).elementCreationFailed(isA(Class.class),
				isA(Exception.class));
	}

	@Test
	public void shouldCreateOnlyOneContainerForMultipleBuildInvocations() {
		SpringAnnotationBuilder builderAnnotated = new SpringAnnotationBuilder(
				AnnotatedUsingStepsAndSpring.class);
		builderAnnotated.buildConfiguration();
		ApplicationContext applicationContext = builderAnnotated
				.applicationContext();
		builderAnnotated.buildConfiguration();
		assertThat(builderAnnotated.applicationContext(),
				sameInstance(applicationContext));
	}

	@Configure()
	@UsingSpring(resources = {
			"org/jbehave/core/configuration/spring/configuration.xml",
			"org/jbehave/core/steps/spring/steps.xml",
			"org/jbehave/core/steps/spring/steps-with-dependency.xml" })
	private static class AnnotatedUsingSpring {

	}

	@Configure()
	@UsingSteps(instances = { FooSteps.class })
	@UsingSpring(resources = { "org/jbehave/core/configuration/spring/configuration.xml" })
	private static class AnnotatedUsingStepsAndSpring {

	}

	@Configure()
	@UsingSpring(resources = { "org/jbehave/core/configuration/spring/configuration.xml" })
	private static class ParentAnnotatedUsingSpring {

	}

	@UsingSteps(instances = { FooSteps.class })
	private static class InheritingAnnotatedUsingSteps extends
			ParentAnnotatedUsingSpring {

	}

	@Configure()
	@UsingSpring()
	private static class AnnotatedWithoutResources {

	}

	@Configure()
	@UsingSpring(resources = { "inexistent" }, ignoreContextFailure = false)
	private static class AnnotatedWithUnloadableResource {

	}

	private static class NotAnnotated {

	}

}
