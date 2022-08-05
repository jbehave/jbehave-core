package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.jbehave.core.steps.JBehaveMatchers.step;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.IGNORABLE;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import org.jbehave.core.annotations.Conditional;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.condition.ReflectionBasedStepConditionMatcher;
import org.jbehave.core.condition.StepConditionMatchException;
import org.jbehave.core.condition.StepConditionMatcher;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.Keywords.StartingWordNotFound;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.AllStepCandidates;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractStepResult.NotPerformed;
import org.jbehave.core.steps.AbstractStepResult.Pending;
import org.jbehave.core.steps.AbstractStepResult.Successful;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.context.StepsContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

class StepCandidateBehaviour {

    private Map<String, String> namedParameters = new HashMap<>();
    private Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
    private Keywords keywords = new LocalizedKeywords();

    private StepCandidate candidateWith(String patternAsString, StepType stepType, Method method, Object instance) {
        return candidateWith(patternAsString, stepType, method, instance, new ParameterControls());
    }

    private StepCandidate candidateWith(String patternAsString, StepType stepType, Method method, Object instance,
            ParameterControls parameterControls) {
        Class<?> stepsType = instance.getClass();
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(configuration, instance);
        return new StepCandidate(patternAsString, 0, stepType, method, stepsType, stepsFactory, new StepsContext(),
                keywords, new RegexPrefixCapturingPatternParser(), configuration.parameterConverters(),
                parameterControls);
    }
    
    @Test
    void shouldMatchStepWithoutParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("I laugh", GIVEN, method, new SomeSteps());
        assertThat(candidate.matches("Given I laugh"), is(true));
    }

    @Test
    void shouldMatchStepWithParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("windows on the $nth floor", WHEN, method, new SomeSteps());
        assertThat(candidate.matches("When windows on the 1st floor"), is(true));
        assertThat(candidate.matches("When windows on the 1st floor are open"), is(not(true)));
    }

    @Test
    void shouldMatchAndStepOnlyWithPreviousStep() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("windows on the $nth floor", WHEN, method, new SomeSteps());
        assertThat(candidate.matches("And windows on the 1st floor"), is(not(true)));
        assertThat(candidate.matches("And windows on the 1st floor", "When windows on the 1st floor"), is(true));
    }

    @Test
    void shouldMatchMultilineStep() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("the grid should look like $grid", THEN, method, new SomeSteps());
        assertThat(candidate.matches("Then the grid should look like \n....\n....\n"), is(true));
    }

    @Test
    void shouldMatchStepWithEmptyParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("methodWith", String.class);
        SomeSteps someSteps = new SomeSteps();
        StepCandidate candidate = candidateWith("windows on the $nth floor", WHEN, method, someSteps);
        String stepAsString = "When windows on the  floor";
        assertThat(candidate.matches(stepAsString), is(true));
        performStep(candidate, stepAsString);
        Object args = someSteps.args;
        assertThat(args, instanceOf(String.class));
        assertThat(((String)args), equalTo(""));
    }

    @Test
    void shouldMatchStepWithEmptyExamplesTableParameter() throws Exception {
        Method method = SomeSteps.class.getMethod("methodWithExamplesTable", ExamplesTable.class);
        SomeSteps someSteps = new SomeSteps();
        StepCandidate candidate = candidateWith("windows attributes:$attrs", WHEN, method, someSteps);
        String stepAsString = "When windows attributes:";
        assertThat(candidate.matches(stepAsString), is(true));
        performStep(candidate, stepAsString);
        Object args = someSteps.args;
        assertThat(args, instanceOf(ExamplesTable.class));
        assertThat(((ExamplesTable)args).asString(), equalTo(""));
    }

    @Test
    void shouldIgnoreStep() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("", IGNORABLE, method, new SomeSteps());
        assertThat(candidate.ignore("!-- Then ignore me"), is(true));
    }

    @Test
    void shouldComment() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("", IGNORABLE, method, new SomeSteps());
        assertThat(candidate.comment("!-- comment"), is(true));
    }

    @Test
    void shouldNotMatchOrIgnoreStepWhenStartingWordNotFound() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        Keywords keywords = new LocalizedKeywords() {
            
            @Override
            public String startingWordFor(StepType stepType) {
                throw new StartingWordNotFound(stepType, new HashMap<StepType, String>());
            }
            
        };
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath(),
                new TableTransformers());
        StepCandidate candidate = new StepCandidate("windows on the $nth floor", 0, WHEN, method, null, null,
                new StepsContext(), keywords, new RegexPrefixCapturingPatternParser(), parameterConverters,
                new ParameterControls());
        assertThat(candidate.matches("When windows on the 1st floor"), is(false));
        assertThat(candidate.ignore("!-- windows on the 1st floor"), is(false));
    }

    @Test
    void shouldProvideStepPriority() throws Exception {
        Method method = SomeSteps.class.getMethod("method");
        StepCandidate candidate = candidateWith("I laugh", GIVEN, method, new SomeSteps());
        assertThat(candidate.getPriority(), equalTo(0));
    }

    @Test
    void shouldCreatePerformableStepUsingTheMatchedString() throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("methodWith", String.class);
        StepCandidate candidate = candidateWith("I live on the $nth floor", THEN, method, someSteps);
        performStep(candidate, "Then I live on the 1st floor");
        assertThat((String) someSteps.args, equalTo("1st"));
    }

    @Test
    void shouldCreatePerformableStepUsingTheMatchedStringAndNamedParameterWithPartialValue() throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("methodWith", String.class);
        StepCandidate candidate = candidateWith("I live on the $nth floor", THEN, method, someSteps);
        namedParameters.put("number", "1");
        performStep(candidate, "Then I live on the <number>st floor");
        assertThat((String) someSteps.args, equalTo("1st"));
    }

    @Test
    void shouldCreatePerformableStepUsingTheMatchedStringAndMultilinedNamedParameterWithPartialValue()
            throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("methodWith", String.class);
        StepCandidate candidate = candidateWith("I live at$address", THEN, method, someSteps);
        namedParameters.put("houseNumber", "221b");
        namedParameters.put("zipCode", "NW1 6XE");
        performStep(candidate, "Then I live at" + System.lineSeparator() + "<houseNumber> Baker St,"
                + System.lineSeparator() + "Marylebone, London <zipCode>, UK");
        assertThat((String) someSteps.args, equalTo(
                System.lineSeparator() + "221b Baker St," + System.lineSeparator() + "Marylebone, London NW1 6XE, UK"));
    }

    @Test
    void shouldCreatePerformableStepWithResultThatDescribesTheStepPerformed() throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("methodWith", String.class);
        StepCandidate candidate = candidateWith("I live on the $nth floor", THEN, method, someSteps);
        String stepAsString = "Then I live on the 1st floor";
        StoryReporter reporter = mock(StoryReporter.class);
        StepResult result = candidate.createMatchedStep(stepAsString, namedParameters, emptyList())
                .perform(reporter, null);
        result.describeTo(reporter);
        verifyBeforeExecutableStep(reporter, stepAsString);
        verify(reporter).successful(
                "Then I live on the " + PARAMETER_VALUE_START + "1st" + PARAMETER_VALUE_END + " floor");
    }
    
    @Test
    void shouldConvertStringParameterValueToUseSystemNewline() throws Exception {
        String windowsNewline = "\r\n";
        String unixNewline = "\n";
        String systemNewline = System.getProperty("line.separator");
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("methodWith", String.class);
        StepCandidate candidate = candidateWith("the grid should look like $grid", THEN, method, someSteps);
        performStep(candidate,
                "Then the grid should look like" + windowsNewline + ".." + unixNewline + ".." + windowsNewline);
        assertThat((String) someSteps.args, equalTo(".." + systemNewline + ".." + systemNewline));
    }

    @Test
    void shouldConvertParameterToNumber() throws Exception {
        assertThatNumberIsConverted(int.class, 14);
        assertThatNumberIsConverted(long.class, 14L);
        assertThatNumberIsConverted(float.class, 14f);
        assertThatNumberIsConverted(double.class, 14d);
    }

    private <T> void assertThatNumberIsConverted(Class<T> type, T number) throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("methodWith", type);
        StepCandidate candidate = candidateWith("I should live in no. $no", THEN, method, someSteps);
        performStep(candidate, "Then I should live in no. 14");
        assertThat((T) someSteps.args, equalTo(number));
    }

    @Test
    void shouldConvertParameterToListOfNumbersOrStrings() throws Exception {
        assertThatListIsConverted("methodWithListOfIntegers", "1,2,3", asList(1, 2, 3));
        assertThatListIsConverted("methodWithListOfLongs", "1,2,3", asList(1L, 2L, 3L));
        assertThatListIsConverted("methodWithListOfFloats", "1.1,2.2,3.3", asList(1.1f, 2.2f, 3.3f));
        assertThatListIsConverted("methodWithListOfDoubles", "1.1,2.2,3.3", asList(1.1d, 2.2d, 3.3d));
        assertThatListIsConverted("methodWithListOfStrings", "1,2,3", asList("1", "2", "3"));
    }

    private void assertThatListIsConverted(String methodName, String csv, List<?> numbers) throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.methodFor(methodName);
        StepCandidate candidate = candidateWith("windows on the $nth floors", WHEN, method, someSteps);
        performStep(candidate, "When windows on the " + csv + " floors");
        assertThat(someSteps.args, equalTo((Object) numbers));
    }

    @Test
    void shouldMatchMethodParametersByAnnotatedNamesInNaturalOrder() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        performStep(candidate, "When I live on the first floor but some call it the ground");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    void shouldMatchMethodParametersByAnnotatedNamesInverseOrder() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInInverseOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        performStep(candidate, "When I live on the first floor but some call it the ground");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    void shouldCreateStepFromTableValuesViaAnnotations() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        String stepAsString = "When I live on the <ith> floor but some call it the <nth>";
        StoryReporter reporter = mock(StoryReporter.class);
        StepResult result = candidate.createMatchedStep(stepAsString, namedParameters, emptyList())
                .perform(reporter, null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
        result.describeTo(reporter);
        verifyBeforeExecutableStep(reporter, stepAsString);
        verify(reporter).successful("When I live on the " + PARAMETER_VALUE_START + "first" + PARAMETER_VALUE_END
                + " floor but some call it the " + PARAMETER_VALUE_START + "ground" + PARAMETER_VALUE_END);
    }

    @Test
    void shouldCreateStepFromTableValuesViaAnnotationsWithCustomParameterDelimiters() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps,
                new ParameterControls().useNameDelimiterLeft("[").useNameDelimiterRight("]"));
        String stepAsString = "When I live on the [ith] floor but some call it the [nth]";
        StoryReporter reporter = mock(StoryReporter.class);
        StepResult result = candidate.createMatchedStep(stepAsString, namedParameters, emptyList())
                .perform(reporter, null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
        result.describeTo(reporter);
        verifyBeforeExecutableStep(reporter, stepAsString);
        verify(reporter).successful("When I live on the " + PARAMETER_VALUE_START + "first" + PARAMETER_VALUE_END
                + " floor but some call it the " + PARAMETER_VALUE_START + "ground" + PARAMETER_VALUE_END);
    }


    @Test
    void shouldMatchMethodParametersByAnnotatedNamesInNaturalOrderForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        performStep(candidate, "When I live on the first floor but some call it the ground");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    void shouldMatchMethodParametersByAnnotatedNamesInverseOrderForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInInverseOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        performStep(candidate, "When I live on the first floor but some call it the ground");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    void shouldCreateStepFromTableValuesViaAnnotationsForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        performStep(candidate, "When I live on the <ith> floor but some call it the <nth>");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    void shouldMatchMethodParametersByParanamerNamesInNaturalOrder() throws Exception {
        shouldMatchMethodParametersByParanamerSomeOrder("methodWithNamedParametersInNaturalOrder");
    }

    @Test
    void shouldMatchMethodParametersByParanamerInverseOrder() throws Exception {
        shouldMatchMethodParametersByParanamerSomeOrder("methodWithNamedParametersInInverseOrder");
    }

    private void shouldMatchMethodParametersByParanamerSomeOrder(String methodName) throws IntrospectionException {
        ParanamerNamedParameterSteps steps = new ParanamerNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor(methodName, ParanamerNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.useParanamer(paranamer);
        performStep(candidate, "When I live on the first floor but some call it the ground");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    void shouldCreateStepFromTableValuesViaParanamer() throws Exception {
        ParanamerNamedParameterSteps steps = new ParanamerNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", ParanamerNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.useParanamer(paranamer);
        performStep(candidate, "When I live on the <ith> floor but some call it the <nth>");
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }
    
    @Test
    void shouldCreateStepFromTableValuesWhenHeadersDoNotMatchParameterNames() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        // I speak LolCatz and mispell headerz
        namedParameters.put("itz", "first");
        namedParameters.put("ntz", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        String stepAsString = "When I live on the <ith> floor but some call it the <nth>";
        Step step = candidate.createMatchedStep(stepAsString, namedParameters, Collections.<Step>emptyList());
        StoryReporter reporter = mock(StoryReporter.class);
        StepResult perform = step.perform(reporter, null);
        assertThat(perform, instanceOf(AbstractStepResult.Pending.class));
        assertThat(perform.parametrisedStep(), equalTo(stepAsString));
        verifyBeforeExecutableStep(reporter, stepAsString);
        StepResult doNotPerform = step.doNotPerform(reporter, null);
        assertThat(doNotPerform, instanceOf(NotPerformed.class));
        assertThat(doNotPerform.parametrisedStep(), equalTo(stepAsString));
        verify(reporter).beforeStep(step(StepExecutionType.NOT_PERFORMED, stepAsString));
        verifyNoMoreInteractions(reporter);
    }

    @Test
    void shouldCreateStepsOfDifferentTypesWithSameMatchingPattern() {
        NamedTypeSteps steps = new NamedTypeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        StepCandidate step0 = candidateMatchingStep(candidates, "Given foo named $name");
        performStep(step0, "Given foo named xyz");
        performStep(step0, "And foo named xyz");
        StepCandidate step1 = candidateMatchingStep(candidates, "When foo named $name");
        performStep(step1, "When foo named Bar");
        performStep(step1, "And foo named Bar");
        assertThat(steps.givenName, equalTo("xyz"));
        assertThat(steps.givenTimes, equalTo(2));
        assertThat(steps.whenName, equalTo("Bar"));
        assertThat(steps.whenTimes, equalTo(2));
    }

    private void performStep(StepCandidate candidate, String stepAsString) {
        StoryReporter reporter = mock(StoryReporter.class);
        candidate.createMatchedStep(stepAsString, namedParameters, emptyList()).perform(reporter, null);
        verifyBeforeExecutableStep(reporter, stepAsString);
    }

    @Test
    void shouldCaptureOutcomeFailures() {
        FailingSteps steps = new FailingSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(1));
        String stepAsString = "When outcome fails for Bar upon verification";
        StoryReporter reporter = mock(StoryReporter.class);
        StepResult stepResult = candidates.get(0).createMatchedStep(stepAsString,
                namedParameters, Collections.<Step>emptyList()).perform(reporter, null);
        UUIDExceptionWrapper failure = stepResult.getFailure();
        assertThat(failure.getCause(), instanceOf(OutcomesFailed.class));
        assertThat(failure.getMessage(), equalTo(stepAsString));
        verifyBeforeExecutableStep(reporter, stepAsString);
    }

    @Test
    void shouldRestart() {
        RestartingSteps steps = new RestartingSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(1));
        String stepAsString = "When blah Bar blah";
        StoryReporter reporter = mock(StoryReporter.class);
        try {
            candidates.get(0).createMatchedStep(stepAsString, namedParameters, Collections.<Step>emptyList())
                    .perform(reporter, null);
            throw new AssertionError("should have barfed");
        } catch (RestartingScenarioFailure e) {
            assertThat(e.getMessage(), is(equalTo("Bar restarting")));
            verifyBeforeExecutableStep(reporter, stepAsString);
        }
    }

    @Test
    void shouldPerformStepsInDryRunMode() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.storyControls().doDryRun(true);
        NamedTypeSteps steps = new NamedTypeSteps(configuration);
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        StepCandidate step0 = candidateMatchingStep(candidates, "Given foo named $name");
        StoryReporter reporter = mock(StoryReporter.class);
        String stepAsString00 = "Given foo named xyz";
        step0.createMatchedStep(stepAsString00, namedParameters, emptyList()).perform(reporter, null);
        String stepAsString01 = "And foo named xyz";
        step0.createMatchedStep(stepAsString01, namedParameters, emptyList()).perform(reporter, null);
        StepCandidate step1 = candidateMatchingStep(candidates, "When foo named $name");
        String stepAsString10 = "When foo named Bar";
        step1.createMatchedStep(stepAsString10, namedParameters, emptyList()).perform(reporter, null);
        String stepAsString11 = "And foo named Bar";
        step1.createMatchedStep(stepAsString11, namedParameters, emptyList()).perform(reporter, null);
        assertThat(steps.givenName, nullValue());
        assertThat(steps.givenTimes, equalTo(0));
        assertThat(steps.whenName, nullValue());
        assertThat(steps.whenTimes, equalTo(0));
        verifyBeforeExecutableStep(reporter, stepAsString00);
        verifyBeforeExecutableStep(reporter, stepAsString01);
        verifyBeforeExecutableStep(reporter, stepAsString10);
        verifyBeforeExecutableStep(reporter, stepAsString11);
    }
    
    @Test
    void shouldMatchAndIdentifyPendingAnnotatedSteps() {
        PendingSteps steps = new PendingSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        StepCandidate pending = candidateMatchingStep(candidates, "Given a pending step");
        assertThat(pending, notNullValue());
        assertThat(pending.isPending(), is(true));
        StepCandidate nonPending = candidateMatchingStep(candidates, "Given a non pending step");
        assertThat(nonPending, notNullValue());
        assertThat(nonPending.isPending(), is(false));
    }

    @Test
    void shouldNotCreateStepOfWrongType() {
        NamedTypeSteps steps = new NamedTypeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        StepCandidate step = candidateMatchingStep(candidates, "Given foo named $name");
        StoryReporter reporter = mock(StoryReporter.class);
        String stepAsString0 = "Given foo named xyz";
        step.createMatchedStep(stepAsString0, namedParameters, emptyList()).perform(reporter, null);
        assertThat(steps.givenName, equalTo("xyz"));
        assertThat(steps.whenName, nullValue());
        verifyBeforeExecutableStep(reporter, stepAsString0);
        assertThrows(StartingWordNotFound.class,
                () -> step.createMatchedStep("Then foo named xyz", namedParameters, emptyList()));
    }

    @Test
    void shouldCreateConditionalMatchedStep() {
        ConditionalSteps instance = new ConditionalSteps();
        ConditionalStepCandidate candidate = createConditionalCandidate(instance);
        StepMonitor monitor = mock(StepMonitor.class);
        candidate.getStepCreator().useStepMonitor(monitor);
        Step step = candidate.createMatchedStep("Given conditional step", Collections.emptyMap(),
                Collections.emptyList());
        StoryReporter reporter = mock(StoryReporter.class);
        TestConditionWithState.state = "strange";
        StepResult result = step.perform(reporter, null);
        assertThat(result, instanceOf(Successful.class));
        TestConditionWithState.state = "things";
        result = step.perform(reporter, null);
        assertThat(result, instanceOf(Successful.class));
        assertEquals("strange things", instance.getPhrase());
        result = step.doNotPerform(reporter, null);
        assertThat(result, instanceOf(NotPerformed.class));
        verify(monitor).beforePerforming(eq("Given conditional step"), eq(false), argThat(matchMethod("addStrange")));
        verify(monitor).beforePerforming(eq("Given conditional step"), eq(false), argThat(matchMethod("addThings")));
        verify(monitor).afterPerforming(eq("Given conditional step"), eq(false), argThat(matchMethod("addStrange")));
        verify(monitor).afterPerforming(eq("Given conditional step"), eq(false), argThat(matchMethod("addThings")));
        verifyNoMoreInteractions(monitor);
    }

    private ArgumentMatcher<Method> matchMethod(String name) {
        return m -> m.getName().contains(name);
    }

    @Test
    void shouldNotAllowInvokeGetMethodOnConditionalStep() {
        ConditionalStepCandidate candidate = createConditionalCandidate();
        assertThrows(UnsupportedOperationException.class, candidate::getMethod);
    }

    @Test
    void shouldReturnPendingIfNoStepsWereMatchedCondition() {
        ConditionalStepCandidate candidate = createConditionalCandidate();
        TestConditionWithState.state = "disable matching of any condition";
        StoryReporter reporter = mock(StoryReporter.class);
        Step step = candidate.createMatchedStep("Given conditional step", Collections.emptyMap(),
                Collections.emptyList());
        String expectedMessage = "Given conditional step" + System.lineSeparator()
                + "None of the following steps were matched any condition:" + System.lineSeparator()
                + "class org.jbehave.core.steps.StepCandidateBehaviour$ConditionalSteps.addThings (condition: class"
                + " org.jbehave.core.steps.StepCandidateBehaviour$TestConditionWithState things)"
                + System.lineSeparator() + "class org.jbehave.core.steps.StepCandidateBehaviour$ConditionalSteps"
                + ".addStrange (condition: class org.jbehave.core.steps.StepCandidateBehaviour$TestConditionWithState"
                + " strange)";
        verifyPending(step, expectedMessage, reporter);
    }

    @Test
    void shouldReturnPendingIfMoreThanOneStepWasMatchedCondition() {
        ConditionalStepCandidate candidate = createConditionalCandidate();
        TestConditionWithState.state = "s";
        StoryReporter reporter = mock(StoryReporter.class);
        Step step = candidate.createMatchedStep("Given conditional step", Collections.emptyMap(),
                Collections.emptyList());
        String expectedMessage = "Given conditional step" + System.lineSeparator()
                + "More than one conditional step matched the condition:" + System.lineSeparator()
                + "class org.jbehave.core.steps.StepCandidateBehaviour$ConditionalSteps.addThings (condition: class"
                + " org.jbehave.core.steps.StepCandidateBehaviour$TestConditionWithState things)"
                + System.lineSeparator() + "class org.jbehave.core.steps.StepCandidateBehaviour$ConditionalSteps"
                + ".addStrange (condition: class org.jbehave.core.steps.StepCandidateBehaviour$TestConditionWithState"
                + " strange)";
        verifyPending(step, expectedMessage, reporter);
    }

    @Test
    void shouldReturnPendingInCaseOfStepConditionMatchException() throws StepConditionMatchException {
        ConditionalSteps instance = new ConditionalSteps();
        StepConditionMatcher matcher = mock(StepConditionMatcher.class);
        StepConditionMatchException matchException = new StepConditionMatchException("match exception message");
        doThrow(matchException).when(matcher).matches(any(), any());
        ConditionalStepCandidate candidate = createConditionalCandidate(instance, matcher);
        StoryReporter reporter = mock(StoryReporter.class);
        Step step = candidate.createMatchedStep("Given conditional step", Collections.emptyMap(),
                Collections.emptyList());
        verifyPending(step, "Given conditional step" + System.lineSeparator() + matchException.getMessage(), reporter);
    }

    @Test
    void shouldReturnPendingInCaseOfStepConditionMatchExceptionOnDoNotPerform() throws StepConditionMatchException {
        ConditionalSteps instance = new ConditionalSteps();
        StepConditionMatcher matcher = mock(StepConditionMatcher.class);
        StepConditionMatchException matchException = new StepConditionMatchException("match exception message");
        doThrow(matchException).when(matcher).matches(any(), any());
        ConditionalStepCandidate candidate = createConditionalCandidate(instance, matcher);
        StoryReporter reporter = mock(StoryReporter.class);
        Step step = candidate.createMatchedStep("Given conditional step", Collections.emptyMap(),
                Collections.emptyList());
        Pending pending = asPending(step.doNotPerform(reporter, null));
        assertEquals("Given conditional step" + System.lineSeparator() + matchException.getMessage(),
                pending.getFailure().getMessage());
    }

    private void verifyPending(Step step, String message, StoryReporter reporter) {
        Pending pending = asPending(step.perform(reporter, null));
        verify(reporter).beforeStep(argThat(s -> message.equals(s.getStepAsString())
                && StepExecutionType.PENDING.equals(s.getExecutionType())));
        assertEquals(message, pending.getFailure().getMessage());
    }

    private Pending asPending(StepResult result) {
        assertThat(result, instanceOf(Pending.class));
        return (Pending) result;
    }

    private ConditionalStepCandidate createConditionalCandidate() {
        return createConditionalCandidate(new ConditionalSteps());
    }

    private ConditionalStepCandidate createConditionalCandidate(Steps instance) {
        return createConditionalCandidate(instance, new ReflectionBasedStepConditionMatcher());
    }

    private ConditionalStepCandidate createConditionalCandidate(Steps instance, StepConditionMatcher matcher) {
        AllStepCandidates allCandidates = new AllStepCandidates(matcher, Collections.singletonList(instance));
        return (ConditionalStepCandidate) allCandidates.getRegularSteps().get(0);
    }

    static class ConditionalSteps extends Steps {

        private String phrase = "";

        @Conditional(condition = TestConditionWithState.class, value = "strange")
        @Given("conditional step")
        public void addStrange() {
            this.phrase += "strange";
            this.phrase += " ";
        }

        @Conditional(condition = TestConditionWithState.class, value = "things")
        @Given("conditional step")
        public void addThings() {
            this.phrase += "things";
        }

        String getPhrase() {
            return this.phrase;
        }

    }

    public static class TestConditionWithState implements Predicate<Object> {

        static String state;

        @Override
        public boolean test(Object t) {
            return t.toString().contains(state);
        }

    }

    static class NamedTypeSteps extends Steps {
        String givenName;
        String whenName;
        int givenTimes;
        int whenTimes;

        public NamedTypeSteps() {
            this(new MostUsefulConfiguration());
        }

        public NamedTypeSteps(Configuration configuration) {
            super(configuration);
        }

        @Given("foo named $name")
        public void givenFoo(String name) {
            givenName = name;
            givenTimes++;
        }

        @When("foo named $name")
        public void whenFoo(String name) {
            whenName = name;
            whenTimes++;
        }

    }

    static class FailingSteps extends Steps {

        @When("outcome fails for $name upon verification")
        public void whenOutcomeFails(String name) {
            OutcomesTable outcomes = new OutcomesTable();
            outcomes.addOutcome("failing", name, equalTo(""));
            outcomes.verify();
        }

    }

    static class RestartingSteps extends Steps {

        @When("blah $name blah")
        public void whenOutcomeFails(String name) {
            throw new RestartingScenarioFailure(name + " restarting");
        }

    }

    static class PendingSteps extends Steps {

        @Given("a pending step")
        @org.jbehave.core.annotations.Pending
        public void pendingStep() {
        }

        @Given("a non pending step")
        public void nonPendingStep() {
        }

    }

    static StepCandidate candidateMatchingStep(List<StepCandidate> candidates, String stepAsString) {
        for (StepCandidate candidate : candidates) {
            if (candidate.matches(stepAsString)) {
                return candidate;
            }
        }
        return null;
    }    


    static Method stepMethodFor(String methodName, Class<? extends Steps> stepsClass) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(stepsClass);
        for (MethodDescriptor md : beanInfo.getMethodDescriptors()) {
            if (md.getMethod().getName().equals(methodName)) {
                return md.getMethod();
            }
        }
        return null;
    }

    private void verifyBeforeExecutableStep(StoryReporter storyReporter, String stepAsString) {
        verify(storyReporter).beforeStep(step(StepExecutionType.EXECUTABLE, stepAsString));
    }

}
