package org.jbehave.core.steps;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
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
import org.jbehave.core.steps.AbstractStepResult.*;
import org.jbehave.core.steps.StepCreator.ParameterNotFound;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.context.StepsContext;
import org.jbehave.core.steps.context.StepsContext.ObjectAlreadyStoredException;
import org.jbehave.core.steps.context.StepsContext.ObjectNotStoredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class StepCreatorBehaviour {

    private ParameterConverters parameterConverters = mock(ParameterConverters.class);

    private StepsContext stepsContext = new StepsContext();

    @BeforeEach
    public void setUp() {
        when(parameterConverters.convert("shopping cart", String.class)).thenReturn("shopping cart");
        when(parameterConverters.convert("book", String.class)).thenReturn("book");
        when(parameterConverters.newInstanceAdding(any())).thenReturn(parameterConverters);
    }

    @Test
    void shouldHandleTargetInvocationFailureInBeforeOrAfterStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(configuration, stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory,
                stepsContext, configuration.parameterConverters(), new ParameterControls(), null, new SilentStepMonitor());

        // When
        Method method = SomeSteps.methodFor("aFailingBeforeScenarioMethod");
        StepResult stepResult = stepCreator.createBeforeOrAfterStep(method, Meta.EMPTY).perform(null, null);

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
    void shouldHandleTargetInvocationFailureInParametrisedStep() throws IntrospectionException {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                null, new ParameterControls(), null, new SilentStepMonitor());
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        Method method = SomeSteps.methodFor("aFailingMethod");
        String stepAsString = "When I fail";
        StepResult stepResult = stepCreator.createParametrisedStep(method, stepAsString, "I fail", null,
                Collections.emptyList()).perform(storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldHandleFailureInParametrisedStep() {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                null, new ParameterControls(), null, new SilentStepMonitor());
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        Method method = null;
        String stepAsString = "When I fail";
        StepResult stepResult = stepCreator.createParametrisedStep(method, stepAsString, "I fail", null,
                Collections.emptyList()).perform(storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldFailIfMatchedParametersAreNotFound() {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepMatcher stepMatcher = mock(StepMatcher.class);
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(configuration, stepsInstance);
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext,
                configuration.parameterConverters(), new ParameterControls(), stepMatcher, new SilentStepMonitor());

        // When
        when(stepMatcher.parameterNames()).thenReturn(new String[] {});
        assertThrows(ParameterNotFound.class, () -> stepCreator.matchedParameter("unknown"));
        // Then .. fail as expected
    }

    @Test
    void shouldCreatePendingAsStepResults() {
        // Given
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When I'm pending";
        Step pendingStep = StepCreator.createPendingStep(stepAsString, null);

        // Then
        assertThat(pendingStep.asString(new Keywords()), equalTo(stepAsString));
        assertThat(pendingStep.perform(storyReporter, null), instanceOf(Pending.class));
        verifyBeforeStep(storyReporter, StepExecutionType.PENDING, stepAsString);
        assertThat(pendingStep.doNotPerform(storyReporter, null), instanceOf(Pending.class));
        verifyBeforeStep(storyReporter, StepExecutionType.NOT_PERFORMED, stepAsString);
        verifyNoMoreInteractions(storyReporter);
    }

    @Test
    void shouldCreateIgnorableAsStepResults() {
        // Given
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "!-- Then ignore me";
        Step ignorableStep = StepCreator.createIgnorableStep(stepAsString);

        // Then
        assertThat(ignorableStep.asString(new Keywords()), equalTo(stepAsString));
        assertThat(ignorableStep.perform(storyReporter, null), instanceOf(Ignorable.class));
        verifyBeforeStep(storyReporter, StepExecutionType.IGNORABLE, stepAsString);
        assertThat(ignorableStep.doNotPerform(storyReporter, null), instanceOf(Ignorable.class));
        verifyBeforeStep(storyReporter, StepExecutionType.NOT_PERFORMED, stepAsString);
        verifyNoMoreInteractions(storyReporter);
    }

    @Test
    void shouldCreateCommentAsStepResults() {
        // Given
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "!-- A comment";
        Step comment = StepCreator.createComment(stepAsString);

        // Then
        assertThat(comment.asString(new Keywords()), equalTo(stepAsString));
        assertThat(comment.perform(storyReporter, null), instanceOf(Comment.class));
        verifyBeforeStep(storyReporter, StepExecutionType.COMMENT, stepAsString);
        assertThat(comment.doNotPerform(storyReporter, null), instanceOf(Comment.class));
        verifyBeforeStep(storyReporter, StepExecutionType.NOT_PERFORMED, stepAsString);
        verifyNoMoreInteractions(storyReporter);
    }

    @Test
    void shouldCreateParametrisedStepWithParsedParametersValues() throws Exception {
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
        Map<String, String> parameters = new HashMap<>();
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When I use parameters " + firstParameterValue + " and " + secondParameterValue;
        StepResult stepResult = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                stepAsString, stepAsString, parameters, Collections.emptyList()).perform(storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        String expected = "When I use parameters " + PARAMETER_VALUE_START + firstParameterValue + PARAMETER_VALUE_END
                + " and " + PARAMETER_VALUE_START + secondParameterValue + PARAMETER_VALUE_END;
        assertThat(stepResult.parametrisedStep(), equalTo(expected));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldCreateParametrisedStepWithNamedParametersValues() throws Exception {
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
        Map<String, String> parameters = new HashMap<>();
        parameters.put("theme", firstParameterValue);
        parameters.put("variant", secondParameterValue);
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        when(stepMatcher.parameterNames()).thenReturn(parameters.keySet().toArray(new String[parameters.size()]));
        when(stepMatcher.parameter(1)).thenReturn(parameters.get(firstParameterValue));
        when(stepMatcher.parameter(2)).thenReturn(parameters.get(secondParameterValue));
        String stepAsString = "When I use parameters <theme> and <variant>";
        StepResult stepResult = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                stepAsString, "I use parameters <theme> and <variant>", parameters, Collections.emptyList()).perform(
                storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        String expected = "When I use parameters " + PARAMETER_VALUE_START + firstParameterValue + PARAMETER_VALUE_END
                + " and " + PARAMETER_VALUE_START + secondParameterValue + PARAMETER_VALUE_END;
        assertThat(stepResult.parametrisedStep(), equalTo(expected));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldInvokeBeforeOrAfterStepMethodWithExpectedParametersFromMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(SomeSteps.methodFor("aMethodWithANamedParameter"),
                new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    void shouldInvokeBeforeOrAfterStepMethodWithMetaUsingParanamer() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        stepCreator.useParanamer(new CachingParanamer(new BytecodeReadingParanamer()));
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(SomeSteps.methodFor("aMethodWithoutNamedAnnotation"),
                new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((String) stepsInstance.args, is("shopping cart"));
    }

    @Test
    void shouldHandleFailureInBeforeOrAfterStepWithMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Method method = SomeSteps.methodFor("aFailingMethod");
        StepResult stepResult = stepCreator.createBeforeOrAfterStep(method, Meta.EMPTY).perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        assertThat(stepResult.getFailure(), instanceOf(UUIDExceptionWrapper.class));
        assertThat(stepResult.getFailure().getCause(), instanceOf(BeforeOrAfterFailed.class));
    }

    @Test
    void shouldInvokeAfterStepUponAnyOutcomeMethodWithExpectedParametersFromMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aMethodWithANamedParameter"),
                AfterScenario.Outcome.ANY, new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    void shouldNotInvokeAfterStepUponSuccessOutcomeMethodIfFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aFailingMethod"),
                AfterScenario.Outcome.SUCCESS, mock(Meta.class));
        StepResult stepResult = stepWithMeta.doNotPerform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Skipped.class));
    }

    @Test
    void shouldInvokeAfterStepUponSuccessOutcomeMethodIfNoFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aMethodWithANamedParameter"),
                AfterScenario.Outcome.SUCCESS, new Meta(properties));
        StepResult stepResult = stepWithMeta.perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    void shouldNotInvokeAfterStepUponFailureOutcomeMethodIfNoFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aFailingMethod"),
                AfterScenario.Outcome.FAILURE, mock(Meta.class));
        StepResult stepResult = stepWithMeta.perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Skipped.class));
    }

    @Test
    void shouldInvokeAfterStepUponFailureOutcomeMethodIfFailureOccurred() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        Properties properties = new Properties();
        properties.put("theme", "shopping cart");
        properties.put("variant", "book");

        // When
        Step stepWithMeta = stepCreator.createAfterStepUponOutcome(SomeSteps.methodFor("aMethodWithANamedParameter"),
                AfterScenario.Outcome.FAILURE, new Meta(properties));
        StepResult stepResult = stepWithMeta.doNotPerform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat(stepsInstance.args, instanceOf(Map.class));

        @SuppressWarnings("unchecked")
        Map<String, String> methodArgs = (Map<String, String>) stepsInstance.args;
        assertThat(methodArgs.get("variant"), is("book"));
        assertThat(methodArgs.get("theme"), is("shopping cart"));
    }

    @Test
    void shouldInvokeBeforeOrAfterStepMethodWithExpectedConvertedParametersFromMeta() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());
        stepCreator.useParanamer(new CachingParanamer(new BytecodeReadingParanamer()));

        // When
        Date aDate = new Date();
        when(parameterConverters.convert(anyString(), eq(Date.class))).thenReturn(aDate);
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(SomeSteps.methodFor("aMethodWithDate"), new Meta());
        StepResult stepResult = stepWithMeta.perform(null, null);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((Date) stepsInstance.args, is(aDate));
    }

    @Test
    void shouldInjectExceptionThatHappenedIfTargetMethodExpectsIt() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(
                SomeSteps.methodFor("aMethodThatExpectsUUIDExceptionWrapper"), mock(Meta.class));
        UUIDExceptionWrapper occurredFailure = new UUIDExceptionWrapper();
        StepResult stepResult = stepWithMeta.perform(null, occurredFailure);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((UUIDExceptionWrapper) stepsInstance.args, is(occurredFailure));
    }

    @Test
    void shouldInjectNoFailureIfNoExceptionHappenedAndTargetMethodExpectsIt() throws Exception {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, mock(StepMatcher.class), new ParameterControls());

        // When
        Step stepWithMeta = stepCreator.createBeforeOrAfterStep(
                SomeSteps.methodFor("aMethodThatExpectsUUIDExceptionWrapper"), mock(Meta.class));
        UUIDExceptionWrapper occurredFailure = new UUIDExceptionWrapper();
        StepResult stepResult = stepWithMeta.perform(null, occurredFailure);

        // Then
        assertThat(stepResult, instanceOf(Silent.class));
        assertThat((UUIDExceptionWrapper) stepsInstance.args, is(occurredFailure));
    }

    @Test
    void shouldMatchParametersByDelimitedNameWithNoNamedAnnotations() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = Collections.singletonMap("param", "value");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<param>");
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When a parameter <param> is set";
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithoutNamedAnnotation"),
                stepAsString, "a parameter <param> is set", params, Collections.emptyList());
        step.perform(storyReporter, null);

        // Then
        assertThat((String) stepsInstance.args, equalTo("value"));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldNotConvertParametersForCompositeSteps() {
        // Given
        SomeSteps stepsInstance = new SomeSteps();
        StepMatcher stepMatcher = mock(StepMatcher.class);
        when(stepMatcher.parameterNames()).thenReturn(new String[] {"name", "num"});
        when(stepMatcher.parameter(1)).thenReturn("value");
        when(stepMatcher.parameter(2)).thenReturn("1");
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "Given a composite step with parameter $name and number $num";
        String stepWithoutStartingWord = "a composite step with parameter $name and number $num";
        Step compositeStep = stepCreator.createParametrisedStep(null, stepAsString, stepWithoutStartingWord,
                Collections.emptyMap(), Collections.emptyList());
        StepResult result = compositeStep.perform(storyReporter, null);

        // Then
        assertThat(result.parametrisedStep(), equalTo(stepAsString));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldMatchParametersByDelimitedNameInKebabCase() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = Collections.singletonMap("pa-ram", "value");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<pa-ram>");
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When a parameter <pa-ram> is set";
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithoutNamedAnnotation"),
                stepAsString, "a parameter <pa-ram> is set", params, Collections.emptyList());
        step.perform(storyReporter, null);

        // Then
        assertThat((String) stepsInstance.args, equalTo("value"));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMatchParametersByDelimitedNameWithDistinctNamedAnnotations() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = new HashMap<>();
        params.put("t", "distinct theme");
        params.put("v", "distinct variant <with non variable inside>");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<t>");
        when(stepMatcher.parameter(2)).thenReturn("<v>");
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When I use parameters <t> and <v>";
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"), stepAsString,
                "I use parameters <t> and <v>", params, Collections.emptyList());
        step.perform(storyReporter, null);

        // Then
        Map<String, String> results = (Map<String, String>) stepsInstance.args;
        assertThat(results.get("theme"), equalTo("distinct theme"));
        assertThat(results.get("variant"), equalTo("distinct variant <with non variable inside>"));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMatchParametersByDelimitedNameWithDistinctNamedAnnotationsWithStoryExampleVariable() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters();
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(true);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = new HashMap<String, String>();
        params.put("t", "distinct theme");
        params.put("v", "distinct variant <with_story_variable_inside>");
        params.put("with_story_variable_inside", "with story variable value");
        when(stepMatcher.parameterNames()).thenReturn(new String[]{"theme", "variant"});
        when(stepMatcher.parameter(1)).thenReturn("<t>");
        when(stepMatcher.parameter(2)).thenReturn("<v>");
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When I use parameters <t> and <v>";
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"), stepAsString,
                "I use parameters <t> and <v>", params, Collections.<Step> emptyList());
        step.perform(storyReporter, null);

        // Then
        Map<String, String> results = (Map<String, String>) stepsInstance.args;
        assertThat(results.get("theme"), equalTo("distinct theme"));
        assertThat(results.get("variant"), equalTo("distinct variant with story variable value"));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldMatchParametersByNamedAnnotationsIfConfiguredToNotUseDelimiterNamedParamters() throws Exception {

        // Given
        SomeSteps stepsInstance = new SomeSteps();
        parameterConverters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        StepMatcher stepMatcher = mock(StepMatcher.class);
        ParameterControls parameterControls = new ParameterControls().useDelimiterNamedParameters(false);
        StepCreator stepCreator = stepCreatorUsing(stepsInstance, stepMatcher, parameterControls);
        Map<String, String> params = new HashMap<>();
        params.put("theme", "a theme");
        params.put("variant", "a variant");
        when(stepMatcher.parameterNames()).thenReturn(params.keySet().toArray(new String[params.size()]));
        when(stepMatcher.parameter(1)).thenReturn("<t>");
        when(stepMatcher.parameter(2)).thenReturn("<v>");
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        String stepAsString = "When I use parameters <t> and <v>";
        Step step = stepCreator.createParametrisedStep(SomeSteps.methodFor("aMethodWithANamedParameter"), stepAsString,
                "I use parameters <t> and <v>", params, Collections.emptyList());
        step.perform(storyReporter, null);

        // Then
        Map<String, String> results = (Map<String, String>) stepsInstance.args;
        assertThat(results.get("theme"), equalTo("a theme"));
        assertThat(results.get("variant"), equalTo("a variant"));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldStoreAndReadObjectsInContext() throws IntrospectionException {
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAString");
        shouldStoreAndReadObjects(methodStoring, true);
    }

    @Test
    void shouldStoreInScenarioAndReadObjectsInContext() throws IntrospectionException {
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAStringInScenario");
        shouldStoreAndReadObjects(methodStoring, true);
    }

    @Test
    void shouldStoreInStoryAndReadObjectsInContext() throws IntrospectionException {
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
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        Method methodRead = SomeSteps.methodFor("aMethodReadingFromContext");
        String stepAsString = "When I store in context";
        StepResult stepResult = stepCreator.createParametrisedStep(methodStoring, stepAsString, "I store in context",
                new HashMap<>(), Collections.emptyList()).perform(storyReporter, null);
        String readStepAsString = "And I read from context";
        StepResult stepResultRead = stepCreator.createParametrisedStep(methodRead, readStepAsString,
                "I read from context", new HashMap<>(), Collections.emptyList()).perform(storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        assertThat(stepResultRead, instanceOf(Successful.class));
        assertThat(stepsInstance.args, instanceOf(String.class));
        assertThat((String) stepsInstance.args, is("someValue"));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, readStepAsString);
    }

    @Test
    void shouldHandleObjectNotStoredFailure() throws IntrospectionException {
        // Given
        setupContext();
        SomeSteps stepsInstance = new SomeSteps();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), stepsInstance);
        StepMatcher stepMatcher = new RegexStepMatcher(StepType.WHEN, "I read from context",
                Pattern.compile("I read from context"), new String[] {});
        StepCreator stepCreator = new StepCreator(stepsInstance.getClass(), stepsFactory, stepsContext, null,
                new ParameterControls(), stepMatcher, new SilentStepMonitor());
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        Method method = SomeSteps.methodFor("aMethodReadingFromContext");
        String stepAsString = "When I read from context";
        StepResult stepResult = stepCreator.createParametrisedStep(method, stepAsString, "I read from context",
                new HashMap<>(), Collections.emptyList()).perform(storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Failed.class));
        Throwable cause = stepResult.getFailure().getCause();
        assertThat(cause, instanceOf(ObjectNotStoredException.class));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void shouldHandleObjectAlreadyStoredFailureInSameLevel() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodStoringAString");
        shouldHandleObjectAlreadyStoredFailure(method);
    }

    @Test
    void shouldHandleObjectAlreadyStoredFailureInDifferentLevel() throws IntrospectionException {
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
        StoryReporter storyReporter = mock(StoryReporter.class);

        // When
        Method method = SomeSteps.methodFor("aMethodStoringAString");
        String stepAsString = "When I store in context";
        StepResult stepResult = stepCreator.createParametrisedStep(method, stepAsString, "I store in context",
                new HashMap<>(), Collections.emptyList()).perform(storyReporter, null);
        String stepAsString2 = "And I store in context";
        StepResult stepResultSecondWrite = stepCreator.createParametrisedStep(duplicateStoreMethod, stepAsString2,
                "I store in context", new HashMap<>(), Collections.emptyList())
                .perform(storyReporter, null);

        // Then
        assertThat(stepResult, instanceOf(Successful.class));
        assertThat(stepResultSecondWrite, instanceOf(Failed.class));
        Throwable cause = stepResultSecondWrite.getFailure().getCause();
        assertThat(cause, instanceOf(ObjectAlreadyStoredException.class));
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString);
        verifyBeforeStep(storyReporter, StepExecutionType.EXECUTABLE, stepAsString2);
    }

    @Test
    void shouldResetKeysBetweenExamples() throws IntrospectionException {
        setupContext();
        Method methodStoring = SomeSteps.methodFor("aMethodStoringAString");

        // First Example
        shouldStoreAndReadObjects(methodStoring, false);

        // Reset Example objects
        stepsContext.resetExample();

        // Second Example
        shouldStoreAndReadObjects(methodStoring, false);
    }

    private void verifyBeforeStep(StoryReporter storyReporter, StepExecutionType type, String stepAsString) {
        verify(storyReporter).beforeStep(argThat(arg -> stepAsString.equals(arg.getStepAsString())
                && type.equals(arg.getExecutionType())));
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
