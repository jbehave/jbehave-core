package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;
import static org.jbehave.core.steps.StepType.GIVEN;
import static org.jbehave.core.steps.StepType.IGNORABLE;
import static org.jbehave.core.steps.StepType.THEN;
import static org.jbehave.core.steps.StepType.WHEN;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractStepResult.NotPerformed;
import org.jbehave.core.steps.AbstractStepResult.Pending;
import org.jbehave.core.steps.CandidateStep.StartingWordNotFound;
import org.junit.Test;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class CandidateStepBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();
    private Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
    private Map<StepType, String> startingWords = new LocalizedKeywords().startingWordsByType();

    private CandidateStep candidateStepWith(String patternAsString, StepType stepType, Method method, Object instance) {
        return new CandidateStep(patternAsString, 0, stepType, method, instance, startingWords,
                new RegexPrefixCapturingPatternParser(), new ParameterConverters());
    }

    @Test
    public void shouldMatchStepWithoutParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        CandidateStep candidateStep = candidateStepWith("I laugh", GIVEN, method, null);
        assertThat(candidateStep.matches("Given I laugh"), is(true));
    }

    @Test
    public void shouldMatchStepWithParameters() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        CandidateStep candidateStep = candidateStepWith("windows on the $nth floor", WHEN, method, null);
        assertThat(candidateStep.matches("When windows on the 1st floor"), is(true));
        assertThat(candidateStep.matches("When windows on the 1st floor are open"), is(not(true)));
    }

    @Test
    public void shouldMatchAndStepOnlyWithPreviousStep() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        CandidateStep candidateStep = candidateStepWith("windows on the $nth floor", WHEN, method, null);
        assertThat(candidateStep.matches("And windows on the 1st floor"), is(not(true)));
        assertThat(candidateStep.matches("And windows on the 1st floor", "When windows on the 1st floor"), is(true));
    }

    @Test
    public void shouldMatchMultilineStep() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        CandidateStep candidateStep = candidateStepWith("the grid should look like $grid", THEN, method, null);
        assertThat(candidateStep.matches("Then the grid should look like \n....\n....\n"), is(true));
    }

    @Test
    public void shouldIgnoreStep() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        CandidateStep candidateStep = candidateStepWith("", IGNORABLE, method, null);
        assertThat(candidateStep.ignore("!-- ignore me"), is(true));
    }

    @Test
    public void shouldNotMatchOrIgnoreStepWhenStartingWordNotFound() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        Map<StepType, String> startingWordsByType = new HashMap<StepType, String>(); // empty list
        CandidateStep candidateStep = new CandidateStep("windows on the $nth floor", 0, WHEN, method, null, startingWordsByType,
                new RegexPrefixCapturingPatternParser(), new ParameterConverters());
        assertThat(candidateStep.matches("When windows on the 1st floor"), is(false));
        assertThat(candidateStep.ignore("!-- windows on the 1st floor"), is(false));
    }

    @Test
    public void shouldProvideStepPriority() throws Exception {
        Method method = SomeSteps.class.getMethod("aMethod");
        CandidateStep candidateStep = candidateStepWith("I laugh", GIVEN, method, null);
        assertThat(candidateStep.getPriority(), equalTo(0));
    }

    @Test
    public void shouldCreatePerformableStepUsingTheMatchedString() throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        CandidateStep candidateStep = candidateStepWith("I live on the $nth floor", THEN, method, someSteps);
        candidateStep.createMatchedStep("Then I live on the 1st floor", tableRow).perform();
        assertThat((String) someSteps.args, equalTo("1st"));
    }

    @Test
    public void shouldCreatePerformableStepWithResultThatDescribesTheStepPerformed() throws Exception {
        StoryReporter reporter = mock(StoryReporter.class);
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", String.class);
        CandidateStep candidateStep = candidateStepWith("I live on the $nth floor", THEN, method, someSteps);
        StepResult result = candidateStep.createMatchedStep("Then I live on the 1st floor", tableRow).perform();
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
        CandidateStep candidateStep = candidateStepWith("the grid should look like $grid", THEN, method, someSteps);
        candidateStep.createMatchedStep(
                "Then the grid should look like" + windowsNewline + ".." + unixNewline + ".." + windowsNewline,
                tableRow).perform();
        assertThat((String) someSteps.args, equalTo(".." + systemNewline + ".." + systemNewline));
    }

    @Test
    public void shouldConvertParameterToNumber() throws Exception {
        assertThatNumberIsConverted("I should live in no. $no", int.class, new Integer(14));
        assertThatNumberIsConverted("I should live in no. $no", long.class, new Long(14L));
        assertThatNumberIsConverted("I should live in no. $no", float.class, new Float(14f));
        assertThatNumberIsConverted("I should live in no. $no", double.class, new Double(14d));
    }

    @SuppressWarnings("unchecked")
    private <T> void assertThatNumberIsConverted(String patternAsString, Class<T> type, T number) throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.class.getMethod("aMethodWith", type);
        CandidateStep candidateStep = candidateStepWith(patternAsString, THEN, method, someSteps);
        candidateStep.createMatchedStep("Then I should live in no. 14", tableRow).perform();
        assertThat((T) someSteps.args, equalTo(number));
    }

    @Test
    public void shouldConvertParameterToListOfNumbersOrStrings() throws Exception {
        assertThatListIsConverted("windows on the $nth floors", "aMethodWithListOfIntegers", int.class, "1,2,3",
                asList(1, 2, 3));
        assertThatListIsConverted("windows on the $nth floors", "aMethodWithListOfLongs", long.class, "1,2,3", asList(
                1L, 2L, 3L));
        assertThatListIsConverted("windows on the $nth floors", "aMethodWithListOfFloats", float.class, "1.1,2.2,3.3",
                asList(1.1f, 2.2f, 3.3f));
        assertThatListIsConverted("windows on the $nth floors", "aMethodWithListOfDoubles", double.class,
                "1.1,2.2,3.3", asList(1.1d, 2.2d, 3.3d));
        assertThatListIsConverted("windows on the $nth floors", "aMethodWithListOfStrings", String.class, "1,2,3",
                asList("1", "2", "3"));
    }

    @SuppressWarnings("unchecked")
    private <T> void assertThatListIsConverted(String patternAsString, String methodName, Class<T> type, String csv,
            List<T> numbers) throws Exception {
        SomeSteps someSteps = new SomeSteps();
        Method method = SomeSteps.methodFor(methodName);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, someSteps);
        candidateStep.createMatchedStep("When windows on the " + csv + " floors", tableRow).perform();
        assertThat((List<T>) someSteps.args, equalTo(numbers));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInNaturalOrder() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.createMatchedStep("When I live on the first floor but some call it the ground", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInverseOrder() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInInverseOrder", AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.createMatchedStep("When I live on the first floor but some call it the ground", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldCreateStepFromTableValuesViaAnnotations() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        tableRow.put("ith", "first");
        tableRow.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.createMatchedStep("When I live on the <ith> floor but some call it the <nth>", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInNaturalOrderForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.createMatchedStep("When I live on the first floor but some call it the ground", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldMatchMethodParametersByAnnotatedNamesInverseOrderForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        String patternAsString = "I live on the $ith floor but some call it the $nth";
        Method method = stepMethodFor("methodWithNamedParametersInInverseOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.createMatchedStep("When I live on the first floor but some call it the ground", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldCreateStepFromTableValuesViaAnnotationsForJsr330Named() throws Exception {
        Jsr330AnnotationNamedParameterSteps steps = new Jsr330AnnotationNamedParameterSteps();
        tableRow.put("ith", "first");
        tableRow.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder",
                Jsr330AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.createMatchedStep("When I live on the <ith> floor but some call it the <nth>", tableRow)
                .perform();
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
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.useParanamer(paranamer);
        candidateStep.createMatchedStep("When I live on the first floor but some call it the ground", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }

    @Test
    public void shouldCreateStepFromTableValuesViaParanamer() throws Exception {
        ParanamerNamedParameterSteps steps = new ParanamerNamedParameterSteps();
        tableRow.put("ith", "first");
        tableRow.put("nth", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", ParanamerNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        candidateStep.useParanamer(paranamer);
        candidateStep.createMatchedStep("When I live on the <ith> floor but some call it the <nth>", tableRow)
                .perform();
        assertThat(steps.ith, equalTo("first"));
        assertThat(steps.nth, equalTo("ground"));
    }
    
    @Test
    public void shouldCreateStepFromTableValuesWhenHeadersDoNotMatchParameterNames() throws Exception {
        AnnotationNamedParameterSteps steps = new AnnotationNamedParameterSteps();
        // I speak LolCatz and mispell headerz
        tableRow.put("itz", "first");
        tableRow.put("ntz", "ground");
        String patternAsString = "I live on the ith floor but some call it the nth";
        Method method = stepMethodFor("methodWithNamedParametersInNaturalOrder", AnnotationNamedParameterSteps.class);
        CandidateStep candidateStep = candidateStepWith(patternAsString, WHEN, method, steps);
        String stepAsString = "When I live on the <ith> floor but some call it the <nth>";
        Step step = candidateStep.createMatchedStep(stepAsString, tableRow);
        StepResult perform = step.perform();
        assertThat(perform, instanceOf(Pending.class));
        assertThat(perform.parametrisedStep(), equalTo(stepAsString));
        StepResult doNotPerform = step.doNotPerform();
        assertThat(doNotPerform, instanceOf(NotPerformed.class));
        assertThat(doNotPerform.parametrisedStep(), equalTo(stepAsString));
    }

    @Test
    public void shouldCreateStepsOfDifferentTypesWithSameMatchingPattern() {
        NamedTypeSteps steps = new NamedTypeSteps();
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(2));
        performStep(candidateSteps.get(0), "Given foo named xyz");
        performStep(candidateSteps.get(0), "And foo named xyz");
        performStep(candidateSteps.get(1), "When foo named Bar");
        performStep(candidateSteps.get(1), "And foo named Bar");
        assertThat(steps.givenName, equalTo("xyz"));
        assertThat(steps.givenTimes, equalTo(2));
        assertThat(steps.whenName, equalTo("Bar"));
        assertThat(steps.whenTimes, equalTo(2));
    }

    private void performStep(CandidateStep candidateStep, String step) {
        candidateStep.createMatchedStep(step, tableRow).perform();
    }

    @Test
    public void shouldCaptureOutcomeFailures() {
        FailingSteps steps = new FailingSteps();
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(1));
        StepResult stepResult = candidateSteps.get(0).createMatchedStep("When outcome fails for Bar upon verification",
                tableRow).perform();
        assertThat(stepResult.getFailure(), Matchers.instanceOf(OutcomesFailed.class));
    }

    @Test
    public void shouldPerformStepsInDryRunMode() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.doDryRun(true);
        NamedTypeSteps steps = new NamedTypeSteps(configuration);
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(2));
        candidateSteps.get(0).createMatchedStep("Given foo named xyz", tableRow).perform();
        candidateSteps.get(0).createMatchedStep("And foo named xyz", tableRow).perform();
        candidateSteps.get(1).createMatchedStep("When foo named Bar", tableRow).perform();
        candidateSteps.get(1).createMatchedStep("And foo named Bar", tableRow).perform();
        assertThat(steps.givenName, nullValue());
        assertThat(steps.givenTimes, equalTo(0));
        assertThat(steps.whenName, nullValue());
        assertThat(steps.whenTimes, equalTo(0));
    }

    @Test(expected = StartingWordNotFound.class)
    public void shouldNotCreateStepOfWrongType() {
        NamedTypeSteps steps = new NamedTypeSteps();
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(2));
        candidateSteps.get(0).createMatchedStep("Given foo named xyz", tableRow).perform();
        assertThat(steps.givenName, equalTo("xyz"));
        assertThat(steps.whenName, nullValue());
        candidateSteps.get(0).createMatchedStep("Then foo named xyz", tableRow).perform();
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
