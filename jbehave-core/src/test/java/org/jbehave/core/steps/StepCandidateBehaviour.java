package org.jbehave.core.steps;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.Keywords.StartingWordNotFound;
import org.jbehave.core.configuration.MostUsefulConfiguration;
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
import org.jbehave.core.steps.context.StepsContext;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;
import static org.jbehave.core.steps.StepType.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StepCandidateBehaviour {

    private Map<String, String> namedParameters = new HashMap<>();
    private Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
    private Keywords keywords = new LocalizedKeywords();

    private StepCandidate candidateWith(String patternAsString, StepType stepType, Method method, Object instance) {
        return candidateWith(patternAsString, stepType, method, instance, new ParameterControls());
    }

    private StepCandidate candidateWith(String patternAsString, StepType stepType, Method method, Object instance, ParameterControls parameterControls) {
        Class<?> stepsType = instance.getClass();
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        InjectableStepsFactory stepsFactory = new InstanceStepsFactory(configuration, instance);
        return new StepCandidate(patternAsString, 0, stepType, method, stepsType, stepsFactory, new StepsContext(),
                keywords, new RegexPrefixCapturingPatternParser(), configuration.parameterConverters(),
                parameterControls);
    }
    
    @Test
    public void shouldMatchStepWithoutParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("I laugh", GIVEN, method, new SomeSteps());
        assertThat(candidate.matches("Given I laugh"), is(true));
    }

    @Test
    public void shouldMatchStepWithParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("windows on the $nth floor", WHEN, method, new SomeSteps());
        assertThat(candidate.matches("When windows on the 1st floor"), is(true));
        assertThat(candidate.matches("When windows on the 1st floor are open"), is(not(true)));
    }

    @Test
    public void shouldMatchAndStepOnlyWithPreviousStep() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("windows on the $nth floor", WHEN, method, new SomeSteps());
        assertThat(candidate.matches("And windows on the 1st floor"), is(not(true)));
        assertThat(candidate.matches("And windows on the 1st floor", "When windows on the 1st floor"), is(true));
    }

    @Test
    public void shouldMatchMultilineStep() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("the grid should look like $grid", THEN, method, new SomeSteps());
        assertThat(candidate.matches("Then the grid should look like \n....\n....\n"), is(true));
    }

    @Test
    public void shouldMatchStepWithEmptyParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        SomeSteps someSteps = new SomeSteps();
        StepCandidate candidate = candidateWith("windows on the $nth floor", WHEN, method, someSteps);
        String stepAsString = "When windows on the  floor";
        assertThat(candidate.matches(stepAsString), is(true));
        Step step = candidate.createMatchedStep(stepAsString, new HashMap<String, String>(), Collections.<Step>emptyList());
        step.perform(null);
        Object args = someSteps.args;
        assertThat(args, instanceOf(String.class));
        assertThat(((String)args), equalTo(""));
    }

    @Test
    public void shouldMatchStepWithEmptyExamplesTableParameter() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethodWithExamplesTable", ExamplesTable.class);
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
    public void shouldIgnoreStep() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("", IGNORABLE, method, new SomeSteps());
        assertThat(candidate.ignore("!-- Then ignore me"), is(true));
    }

    @Test
    public void shouldComment() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("", IGNORABLE, method, new SomeSteps());
        assertThat(candidate.comment("!-- comment"), is(true));
    }

    @Test
    public void shouldNotMatchOrIgnoreStepWhenStartingWordNotFound() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        Keywords keywords = new LocalizedKeywords(){            
            
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
    public void shouldProvideStepPriority() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        StepCandidate candidate = candidateWith("I laugh", GIVEN, method, new SomeSteps());
        assertThat(candidate.getPriority(), equalTo(0));
    }

    @Test
    public void shouldCreatePerformableStepUsingTheMatchedString() throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        StepCandidate candidate = candidateWith("I live on the $nth floor", THEN, method, someSteps);
        candidate.createMatchedStep("Then I live on the 1st floor", namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat((String) someSteps.args, equalTo("1st"));
    }

    @Test
    public void shouldCreatePerformableStepUsingTheMatchedStringAndNamedParameterWithPartialValue() throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        StepCandidate candidate = candidateWith("I live on the $nth floor", THEN, method, someSteps);
        namedParameters.put("number", "1");
        candidate.createMatchedStep("Then I live on the <number>st floor", namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat((String) someSteps.args, equalTo("1st"));
    }

    @Test
    public void shouldCreatePerformableStepUsingTheMatchedStringAndMultilinedNamedParameterWithPartialValue()
            throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        StepCandidate candidate = candidateWith("I live at$address", THEN, method, someSteps);
        namedParameters.put("houseNumber", "221b");
        namedParameters.put("zipCode", "NW1 6XE");
        String stepAsString = "Then I live at" + System.lineSeparator() + "<houseNumber> Baker St,"
                + System.lineSeparator() + "Marylebone, London <zipCode>, UK";
        candidate.createMatchedStep(stepAsString, namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat((String) someSteps.args, equalTo(
                System.lineSeparator() + "221b Baker St," + System.lineSeparator() + "Marylebone, London NW1 6XE, UK"));
    }

    @Test
    public void shouldCreatePerformableStepWithResultThatDescribesTheStepPerformed() throws Exception {
        StoryReporter reporter = mock(StoryReporter.class);
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        StepCandidate candidate = candidateWith("I live on the $nth floor", THEN, method, someSteps);
        StepResult result = candidate.createMatchedStep("Then I live on the 1st floor", namedParameters, Collections.<Step>emptyList()).perform(null);
        result.describeTo(reporter);
        verify(reporter).successful(
                "Then I live on the " + PARAMETER_VALUE_START + "1st" + PARAMETER_VALUE_END + " floor");
    }
    
    @Test
    public void shouldConvertStringParameterValueToUseSystemNewline() throws Exception {
        String windowsNewline = "\r\n";
        String unixNewline = "\n";
        String systemNewline = System.getProperty("line.separator");
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        StepCandidate candidate = candidateWith("the grid should look like $grid", THEN, method, someSteps);
        candidate.createMatchedStep(
                "Then the grid should look like" + windowsNewline + ".." + unixNewline + ".." + windowsNewline,
                namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat((String) someSteps.args, equalTo(".." + systemNewline + ".." + systemNewline));
    }

    @Test
    public void shouldConvertParameterToNumber() throws Exception {
        assertThatNumberIsConverted(int.class, 14);
        assertThatNumberIsConverted(long.class, 14L);
        assertThatNumberIsConverted(float.class, 14f);
        assertThatNumberIsConverted(double.class, 14d);
    }

    private <T> void assertThatNumberIsConverted(Class<T> type, T number) throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", type);
        StepCandidate candidate = candidateWith("I should live in no. $no", THEN, method, someSteps);
        candidate.createMatchedStep("Then I should live in no. 14", namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat((T)someSteps.args, equalTo(number));
    }

    @Test
    public void shouldConvertParameterToListOfNumbersOrStrings() throws Exception {
        assertThatListIsConverted("aMethodWithListOfIntegers", "1,2,3", asList(1, 2, 3));
        assertThatListIsConverted("aMethodWithListOfLongs", "1,2,3", asList(1L, 2L, 3L));
        assertThatListIsConverted("aMethodWithListOfFloats", "1.1,2.2,3.3", asList(1.1f, 2.2f, 3.3f));
        assertThatListIsConverted("aMethodWithListOfDoubles", "1.1,2.2,3.3", asList(1.1d, 2.2d, 3.3d));
        assertThatListIsConverted("aMethodWithListOfStrings", "1,2,3", asList("1", "2", "3"));
    }

    private void assertThatListIsConverted(String methodName, String csv, List<?> numbers) throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.methodFor(methodName);
        StepCandidate candidate = candidateWith("windows on the $nth floors", WHEN, method, someSteps);
        candidate.createMatchedStep("When windows on the " + csv + " floors", namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat(someSteps.args, equalTo((Object)numbers));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInNaturalOrder() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.createMatchedStep("When I live on the first floor but some call it the ground", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInverseOrder() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInInverseOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.createMatchedStep("When I live on the first floor but some call it the ground", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldCreateStepFromTableValuesViaAnnotations() throws Exception {
        StoryReporter reporter = mock(StoryReporter.class);
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        StepResult result =candidate.createMatchedStep("When I live on the <ith> floor but some call it the <nth>", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
        result.describeTo(reporter);
        verify(reporter).successful(
                "When I live on the " + PARAMETER_VALUE_START + "first" + PARAMETER_VALUE_END + " floor but some call it the " + PARAMETER_VALUE_START + "ground" + PARAMETER_VALUE_END );
    }

    @Test
    public void shouldCreateStepFromTableValuesViaAnnotationsWithCustomParameterDelimiters() throws Exception {
        StoryReporter reporter = mock(StoryReporter.class);
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps, new ParameterControls().useNameDelimiterLeft("[").useNameDelimiterRight("]"));
        StepResult result = candidate.createMatchedStep("When I live on the [ith] floor but some call it the [nth]", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
        result.describeTo(reporter);
        verify(reporter).successful(
                "When I live on the " + PARAMETER_VALUE_START + "first" + PARAMETER_VALUE_END + " floor but some call it the " + PARAMETER_VALUE_START + "ground" + PARAMETER_VALUE_END );
    }


    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInNaturalOrderForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.createMatchedStep("When I live on the first floor but some call it the ground", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInverseOrderForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInInverseOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.createMatchedStep("When I live on the first floor but some call it the ground", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldCreateStepFromTableValuesViaAnnotationsForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.createMatchedStep("When I live on the <ith> floor but some call it the <nth>", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldMatchMethodParametersByParanamerNamesInNaturalOrder() throws Exception {
        shouldMatchMethodParametersByParanamerSomeOrder("methodWithNamedParametersInNaturalOrder");
    }

    @Test
    public void shouldMatchMethodParametersByParanamerInverseOrder() throws Exception {
        shouldMatchMethodParametersByParanamerSomeOrder("methodWithNamedParametersInInverseOrder");
    }

    private void shouldMatchMethodParametersByParanamerSomeOrder(String methodName) throws IntrospectionException {
        ParanamerNamedParameterSteps steps = new ParanamerNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor(methodName, ParanamerNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.useParanamer(paranamer);
        candidate.createMatchedStep("When I live on the first floor but some call it the ground", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldCreateStepFromTableValuesViaParanamer() throws Exception {
        ParanamerNamedParameterSteps steps = new ParanamerNamedParameterSteps();
        namedParameters.put("ith", "first");
        namedParameters.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", ParanamerNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        candidate.useParanamer(paranamer);
        candidate.createMatchedStep("When I live on the <ith> floor but some call it the <nth>", namedParameters, Collections.<Step>emptyList())
                .perform(null);
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }
    
    @Test
    public void shouldCreateStepFromTableValuesWhenHeadersDoNotMatchParameterNames() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        // I speak LolCatz and mispell headerz
        namedParameters.put("itz", "first");
        namedParameters.put("ntz", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        StepCandidate candidate = candidateWith(patternAsString, WHEN, method, steps);
        String stepAsString = "When I live on the <ith> floor but some call it the <nth>";
        Step step = candidate.createMatchedStep(stepAsString, namedParameters, Collections.<Step>emptyList());
        StepResult perform = step.perform(null);
        assertThat(perform, instanceOf(AbstractStepResult.Pending.class));
        assertThat(perform.parametrisedStep(), equalTo(stepAsString));
        StepResult doNotPerform = step.doNotPerform(null);
        assertThat(doNotPerform, instanceOf(NotPerformed.class));
        assertThat(doNotPerform.parametrisedStep(), equalTo(stepAsString));
    }

    @Test
    public void shouldCreateStepsOfDifferentTypesWithSameMatchingPattern() {
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

    private void performStep(StepCandidate candidate, String step) {
        candidate.createMatchedStep(step, namedParameters, Collections.<Step>emptyList()).perform(null);
    }

    @Test
    public void shouldCaptureOutcomeFailures() {
        FailingSteps steps = new FailingSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(1));
        String stepAsString = "When outcome fails for Bar upon verification";
        StepResult stepResult = candidates.get(0).createMatchedStep(stepAsString,
                namedParameters, Collections.<Step>emptyList()).perform(null);
        UUIDExceptionWrapper failure = stepResult.getFailure();
        assertThat(failure.getCause(), instanceOf(OutcomesFailed.class));        
        assertThat(failure.getMessage(), equalTo(stepAsString));        
    }

    @Test
    public void shouldRestart() {
        RestartingSteps steps = new RestartingSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(1));
        try {
            candidates.get(0).createMatchedStep("When blah Bar blah", namedParameters, Collections.<Step>emptyList()).perform(null);
            throw new AssertionError("should have barfed");
        } catch (RestartingScenarioFailure e) {
            assertThat(e.getMessage(), is(equalTo("Bar restarting")));
        }
    }

    @Test
    public void shouldPerformStepsInDryRunMode() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.storyControls().doDryRun(true);
        NamedTypeSteps steps = new NamedTypeSteps(configuration);
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        StepCandidate step0 = candidateMatchingStep(candidates, "Given foo named $name");
        step0.createMatchedStep("Given foo named xyz", namedParameters, Collections.<Step>emptyList()).perform(null);
        step0.createMatchedStep("And foo named xyz", namedParameters, Collections.<Step>emptyList()).perform(null);
        StepCandidate step1 = candidateMatchingStep(candidates, "When foo named $name");
        step1.createMatchedStep("When foo named Bar", namedParameters, Collections.<Step>emptyList()).perform(null);
        step1.createMatchedStep("And foo named Bar", namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat(steps.givenName, nullValue());
        assertThat(steps.givenTimes, equalTo(0));
        assertThat(steps.whenName, nullValue());
        assertThat(steps.whenTimes, equalTo(0));
    }
    
    @Test
    public void shouldMatchAndIdentifyPendingAnnotatedSteps() {
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

	@Test(expected = StartingWordNotFound.class)
    public void shouldNotCreateStepOfWrongType() {
        NamedTypeSteps steps = new NamedTypeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        StepCandidate step = candidateMatchingStep(candidates, "Given foo named $name");
        step.createMatchedStep("Given foo named xyz", namedParameters, Collections.<Step>emptyList()).perform(null);
        assertThat(steps.givenName, equalTo("xyz"));
        assertThat(steps.whenName, nullValue());
        step.createMatchedStep("Then foo named xyz", namedParameters, Collections.<Step>emptyList()).perform(null);
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
        @Pending
        public void aPendingStep() {
        }

        @Given("a non pending step")
        public void aNonPendingStep() {
        }

    }

    static StepCandidate candidateMatchingStep(List<StepCandidate> candidates, String stepAsString) {
        for (StepCandidate candidate : candidates) {
            if (candidate.matches(stepAsString)){
                return candidate ;
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

}
