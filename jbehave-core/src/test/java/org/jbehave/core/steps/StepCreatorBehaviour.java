package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStepMatcher;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractStepResult.Comment;
import org.jbehave.core.steps.AbstractStepResult.Failed;
import org.jbehave.core.steps.AbstractStepResult.Ignorable;
import org.jbehave.core.steps.AbstractStepResult.Pending;
import org.jbehave.core.steps.AbstractStepResult.Silent;
import org.jbehave.core.steps.AbstractStepResult.Skipped;
import org.jbehave.core.steps.AbstractStepResult.Successful;
import org.jbehave.core.steps.context.StepsContext;
import org.jbehave.core.steps.context.StepsContext.ObjectAlreadyStoredException;
import org.jbehave.core.steps.context.StepsContext.ObjectNotStoredException;
import org.jbehave.core.steps.StepCreator.ParameterNotFound;
import org.jbehave.core.steps.StepCreator.ParametrisedStep;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;

public class StepCreatorBehaviour {

    private ParameterConverters parameterConverters = mock(ParameterConverters.class);

    private StepsContext stepsContext = new StepsContext();

    @Before
    public void setUp() throws Exception {
        when(parameterConverters.convert("shopping cart", String.class)).thenReturn("shopping cart");
        when(parameterConverters.convert("book", String.class)).thenReturn("book");
        when(parameterConverters.newInstanceAdding(Matchers.<ParameterConverters.ParameterConverter> anyObject()))
                .thenReturn(parameterConverters);
    }

    @Test
    public void shouldHandleTargetInvocationFailureInBeforeOrAfterStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(configuration, stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory,
                stepsContext, configuration.parameterConverters(), new ParameterControls(), null, new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aFailingBeforeScenarioMethod");
        StepResult stepResult = stepCreator.createBeforeOrAfterStep(method, Meta.EMPTY).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        assertThat(stepResult.getFailure(), instanceOf(UUIDExceptionWrapper.class));
        Throwable cause = stepResult.getFailure().getCause();
        assertThat(cause, instanceOf(BeforeOrAfterFailed.class));
        assertThat(
                cause.getMessage(),
                org.hamcrest.Matchers
                        .equalTo("Method aFailingBeforeScenarioMethod (annotated with @BeforeScenario in class org.jbehave.core.steps.SomeSteps) failed: java.lang.RuntimeException"));
    }

    @Test
    public void shouldDescribeStepToReporterBeforeExecutingParametrisedStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                null, new ParameterControls(), null, new SilentStepMonitor());
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        Method method = SomeSteps.methodFor("aMethod");
        ((ParametrisedStep) stepCreator.createParametrisedStep(method, "When I run", "I run", null)).describeTo(storyReporter);

        // Then
        verify(storyReporter).beforeStep("When I run");
    }

    @Test
    public void shouldHandleTargetInvocationFailureInParametrisedStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                null, new ParameterControls(), null, new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aFailingMethod");
        StepResult stepResult = stepCreator.createParametrisedStep(method, "When I fail", "I fail", null).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
    }

    @Test
    public void shouldHandleFailureInParametrisedStep() {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                null, new ParameterControls(), null, new SilentStepMonitor());

        // When
        Method method = null;
        StepResult stepResult = stepCreator.createParametrisedStep(method, "When I fail", "I fail", null).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
    }

    @Test(expected = ParameterNotFound.class)
    public void shouldFailIfMatchedParametersAreNotFound() {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepMatcher stepMatcher = mock(StepMatcher.class);
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(configuration, stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                configuration.parameterConverters(), new ParameterControls(), stepMatcher, new SilentStepMonitor());

        // When
        when(stepMatcher.parameterNames()).thenReturn(new String[] {});
        stepCreator.matchedParameter("unknown");

        // Then .. fail as expected
    }

    @Test
    public void shouldCreatePendingAsStepResults() {
        // When
        String stepAsString = "When I'm pending";
        Step pendingStep = StepCreator.createPendingStep(stepAsString, null);

        // Then
        assertThat(pendingStep.asString(new Keywords()), equalTo(stepAsString));
        assertThat(pendingStep.perform(null), instanceOf(Pending.class));
        assertThat(pendingStep.doNotPerform(null), instanceOf(Pending.class));
    }

    @Test
    public void shouldCreateIgnorableAsStepResults() {
        // When
        String stepAsString = "!-- Then ignore me";
        Step ignorableStep = StepCreator.createIgnorableStep(stepAsString);

        // Then
        assertThat(ignorableStep.asString(new Keywords()), equalTo(stepAsString));
        assertThat(ignorableStep.perform(null), instanceOf(Ignorable.class));
        assertThat(ignorableStep.doNotPerform(null), instanceOf(Ignorable.class));
    }

    @Test
    public void shouldCreateCommentAsStepResults() {
        // When
        String stepAsString = "!-- A comment";
        Step comment = StepCreator.createComment(stepAsString);

        // Then
        assertThat(comment.asString(new Keywords()), equalTo(stepAsString));
        assertThat(comment.perform(null), instanceOf(Comment.class));
        assertThat(comment.doNotPerform(null), instanceOf(Comment.class));
    }

    @Test
    public void shouldCreateParametrisedStepWithParsedParametersValues() throws Exception {
        assertThatParametrisedStepHasMarkedParsedParametersValues("shopping cart", "book");
        assertThatParametrisedStepHasMarkedParsedParametersValues("bookreading", "book");
        assertThatParametrisedStepHasMarkedParsedParametersValues("book", "bookreading");
    }

    private void assertThatParametrisedStepHasMarkedParsedParametersValues(String firstParameterValue,
            String secondParameterValue) throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepMatcher stepMatcher = new RegexStepMatcher(StepType.WHEN, "I use parameters $theme and $variant", Pattern.compile("When I use parameters (.*) and (.*)"), new String[]{"theme", "variant"});
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, new ParameterControls());
        Map<String, String> parameters = new HashMap<String, String>();

        // When
        StepResult stepResult = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                "When I use parameters "+firstParameterValue+" and " + secondParameterValue, "When I use parameters "+firstParameterValue+" and " + secondParameterValue, parameters)
                .perform(null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        String expected = "When I use parameters " + PARAMETER_VALUE_START + firstParameterValue + PARAMETER_VALUE_END
                + " and " + PARAMETER_VALUE_START + secondParameterValue + PARAMETER_VALUE_END;
        assertThat(stepResult.parametrisedStep(), equalTo(expected));
    }

    @Test
    public void shouldCreateParametrisedStepWithNamedParametersValues() throws Exception {
        assertThatParametrisedStepHasMarkedNamedParameterValues("shopping cart", "book");
        assertThatParametrisedStepHasMarkedNamedParameterValues("bookreading", "book");
        assertThatParametrisedStepHasMarkedNamedParameterValues("book", "bookreading");
    }

    private void assertThatParametrisedStepHasMarkedNamedParameterValues(String firstParameterValue,
            String secondParameterValue) throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepMatcher stepMatcher = mock(StepMatcher.class);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, new ParameterControls().useDelimiterNamedParameters(false));
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("theme", firstParameterValue);
        parameters.put("variant", secondParameterValue);

        // When
        when(stepMatcher.parameterNames()).thenReturn(parameters.keySet().toArray(new String[parameters.size()]));
        when(stepMatcher.parameter(1)).thenReturn(parameters.get(firstParameterValue));
        when(stepMatcher.parameter(2)).thenReturn(parameters.get(secondParameterValue));
        StepResult stepResult = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                "When I use parameters <theme> and <variant>", "I use parameters <theme> and <variant>", parameters)
                .perform(null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        String expected = "When I use parameters " + PARAMETER_VALUE_START + firstParameterValue + PARAMETER_VALUE_END
                + " and " + PARAMETER_VALUE_START + secondParameterValue + PARAMETER_VALUE_END;
        assertThat(stepResult.parametrisedStep(), equalTo(expected));
    }

    @Test
    public void shouldInvokeBeforeOrAfterStepMethodWithExpectedParametersFromMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    public void shouldInvokeBeforeOrAfterStepMethodWithMetaUsingParanamer() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        stepCreator.useParanamer(new CachingParanamer(new BytecodeReadingParanamer()));
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(SomeSteps.methodFor("aMethodWithoutNamedAnnotation"),
                new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((String) stepsInstance.args, is("shopping cart"));
    }

    @Test
    public void shouldHandleFailureInBeforeOrAfterStepWithMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Method method = SomeSteps.methodFor("aFailingMethod");
        StepResult stepResult = stepCreator.createBeforeOrAfterStep(method, Meta.EMPTY).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        assertThat(stepResult.getFailure(), instanceOf(UUIDExceptionWrapper.class));
        assertThat(stepResult.getFailure().getCause(), instanceOf(BeforeOrAfterFailed.class));
    }

    @Test
    public void shouldInvokeAfterStepUponAnyOutcomeMethodWithExpectedParametersFromMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aMethodWithANamedParameter"),
                AfterScenario.Outcome.ANY, new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    public void shouldNotInvokeAfterStepUponSuccessOutcomeMethodIfFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aFailingMethod"),
                AfterScenario.Outcome.SUCCESS, mock(Meta.class));
        StepResult stepResult = stepWithMeta.doNotPerform(null);

        // Then
        assertThat(stepResult, instanceOf(Skipped.class));
    }

    @Test
    public void shouldInvokeAfterStepUponSuccessOutcomeMethodIfNoFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aMethodWithANamedParameter"),
                AfterScenario.Outcome.SUCCESS, new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    public void shouldNotInvokeAfterStepUponFailureOutcomeMethodIfNoFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aFailingMethod"),
                AfterScenario.Outcome.FAILURE, mock(Meta.class));
        StepResult stepResult = stepWithMeta.perform(null);

        // Then
        assertThat(stepResult, instanceOf(Skipped.class));
    }

    @Test
    public void shouldInvokeAfterStepUponFailureOutcomeMethodIfFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aMethodWithANamedParameter"),
                AfterScenario.Outcome.FAILURE, new Meta(properties));
        StepResult stepResult = stepWithMeta.doNotPerform(null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    public void shouldInvokeBeforeOrAfterStepMethodWithExpectedConvertedParametersFromMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        stepCreator.useParanamer(new CachingParanamer(new BytecodeReadingParanamer()));

        // When
        Date aDate = new Date();
        when(parameterConverters.convert(anyString(), eq(Date.class))).thenReturn(aDate);
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(SomeSteps.methodFor("aMethodWithDate"), new Meta());
        StepResult stepResult = stepWithMeta.perform(null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((Date) stepsInstance.args, is(aDate));
    }

    @Test
    public void shouldInjectExceptionThatHappenedIfTargetMethodExpectsIt() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(
                SomeSteps.methodFor("aMethodThatExpectsUUIDExceptionWrapper"), mock(Meta.class));
        UUIDExceptionWrapper occurredFailure = new UUIDExceptionWrapper();
        StepResult stepResult = stepWithMeta.perform(occurredFailure);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((UUIDExceptionWrapper) stepsInstance.args, is(occurredFailure));
    }

    @Test
    public void shouldInjectNoFailureIfNoExceptionHappenedAndTargetMethodExpectsIt() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(
                SomeSteps.methodFor("aMethodThatExpectsUUIDExceptionWrapper"), mock(Meta.class));
        UUIDExceptionWrapper occurredFailure = new UUIDExceptionWrapper();
        StepResult stepResult = stepWithMeta.perform(occurredFailure);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((UUIDExceptionWrapper) stepsInstance.args, is(occurredFailure));
    }

    @Test
    public void shouldMatchParametersByDelimitedNameWithNoNamedAnnotations() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = Collections.singletonMap("param", "value");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<param>");

        // When
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithoutNamedAnnotation"),
                "When a parameter <param> is set", "a parameter <param> is set", params);
        step.perform(null);

        // Then
        assertThat((String) stepsInstance.args, equalTo("value"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMatchParametersByDelimitedNameWithDistinctNamedAnnotations() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = new HashMap<String, String>();
        params.put("t", "distinct theme");
        params.put("v", "distinct variant");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<t>");
        when(stepMatcher.parameter(2)).thenReturn("<v>");

        // When
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                "When I use parameters <t> and <v>", "I use parameters <t> and <v>", params);
        step.perform(null);

        // Then
        Map<String, String> results = (Map<String, String>) stepsInstance.args;
        assertThat(results.get("theme"), equalTo("distinct theme"));
        assertThat(results.get("variant"), equalTo("distinct variant"));

    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldMatchParametersByNamedAnnotationsIfConfiguredToNotUseDelimiterNamedParamters() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(false);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = new HashMap<String, String>();
        params.put("theme", "a theme");
        params.put("variant", "a variant");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<t>");
        when(stepMatcher.parameter(2)).thenReturn("<v>");

        // When
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                "When I use parameters <t> and <v>", "I use parameters <t> and <v>", params);
        step.perform(null);

        // Then
        Map<String, String> results = (Map<String, String>) stepsInstance.args;
        assertThat(results.get("theme"), equalTo("a theme"));
        assertThat(results.get("variant"), equalTo("a variant"));
    }

    @Test
    public void shouldStoreAndReadObjectsInContext() throws IntrospectionException {
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAString");
        shouldStoreAndReadObjects(methodStoring, true);
    }

    @Test
    public void shouldStoreInScenarioAndReadObjectsInContext() throws IntrospectionException {
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAStringInScenario");
        shouldStoreAndReadObjects(methodStoring, true);
    }

    @Test
    public void shouldStoreInStoryAndReadObjectsInContext() throws IntrospectionException {
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAStringInStory");
        shouldStoreAndReadObjects(methodStoring, true);
    }

    private void shouldStoreAndReadObjects(Method methodStoring, boolean resetContext) throws IntrospectionException {
        // Given
        if (resetContext) {
            setupContext();
        }
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepMatcher stepMatcher = new RegexStepMatcher(StepType.WHEN, "I read from context",
                Pattern.compile("I read from context"), new String[] {});
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext, null,
                new ParameterControls(), stepMatcher, new SilentStepMonitor());

        // When
        Method methodRead = SomeSteps.methodFor("aMethodReadingFromContext");
        StepResult stepResult = stepCreator.createParametrisedStep(methodStoring, "When I store in context",
                "I store in context", new HashMap<String, String>()).perform(null);
        StepResult stepResultRead = stepCreator.createParametrisedStep(methodRead, "And I read from context",
                "I read from context", new HashMap<String, String>()).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        assertThat(stepResultRead, instanceOf(Successful.class));
        assertThat(stepsInstance.args, instanceOf(String.class));
        assertThat((String) stepsInstance.args, is("someValue"));
    }

    @Test
    public void shouldHandleObjectNotStoredFailure() throws IntrospectionException {
        // Given
        setupContext();
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepMatcher stepMatcher = new RegexStepMatcher(StepType.WHEN, "I read from context",
                Pattern.compile("I read from context"), new String[] {});
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext, null,
                new ParameterControls(), stepMatcher, new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aMethodReadingFromContext");
        StepResult stepResult = stepCreator.createParametrisedStep(method, "When I read from context", "I read from context",
                new HashMap<String, String>()).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        Throwable cause = stepResult.getFailure().getCause();
        assertThat(cause, instanceOf(ObjectNotStoredException.class));
    }

    @Test
    public void shouldHandleObjectAlreadyStoredFailureInSameLevel() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodStoringAString");
        shouldHandleObjectAlreadyStoredFailure(method);
    }

    @Test
    public void shouldHandleObjectAlreadyStoredFailureInDifferentLevel() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodStoringAStringInStory");
        shouldHandleObjectAlreadyStoredFailure(method);
    }

    private void shouldHandleObjectAlreadyStoredFailure(Method duplicateStoreMethod) throws IntrospectionException {
        // Given
        setupContext();
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext, null,
                new ParameterControls(), mock(StepMatcher.class), new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aMethodStoringAString");
        StepResult stepResult = stepCreator.createParametrisedStep(method, "When I store in context", "I store in context",
                new HashMap<String, String>()).perform(null);
        StepResult stepResultSecondWrite = stepCreator.createParametrisedStep(duplicateStoreMethod, "And I store in context",
                "I store in context", new HashMap<String, String>()).perform(null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        assertThat(stepResultSecondWrite, instanceOf(Failed.class));
        Throwable cause = stepResultSecondWrite.getFailure().getCause();
        assertThat(cause, instanceOf(ObjectAlreadyStoredException.class));
    }

    @Test
    public void shouldResetKeysBetweenExamples() throws IntrospectionException {
        setupContext();
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAString");

        // First Example
        shouldStoreAndReadObjects(methodStoring, false);

        // Reset Example objects
        stepsContext.resetExample();

        // Second Example
        shouldStoreAndReadObjects(methodStoring, false);
    }

    private StepCreator stepCreatorUsing(SomeSteps stepsInstance, StepMatcher stepMatcher, ParameterControls parameterControls) {
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        return new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext, parameterConverters,
                parameterControls, stepMatcher, new SilentStepMonitor());
    }

    private void setupContext() {
        stepsContext.resetStory();
        stepsContext.resetScenario();
        stepsContext.resetExample();
    }
}
