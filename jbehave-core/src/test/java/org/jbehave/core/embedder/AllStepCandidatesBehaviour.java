package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Conditional;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.condition.ReflectionBasedStepConditionMatcher;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.CompositeCandidateSteps;
import org.jbehave.core.steps.ConditionalStepCandidate;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;

class AllStepCandidatesBehaviour {

    private static final String CONDITIONAL_STEP = "conditional step";

    private final ReflectionBasedStepConditionMatcher matcher = new ReflectionBasedStepConditionMatcher();

    @Test
    void shouldFailOnDuplicateCandidatesInOneClass() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new DuplicateStepsInOneClass());
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(matcher, candidates));
    }

    @Test
    void shouldFailOnDuplicateCandidatesWithDifferentParameterNames() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new DuplicateStepsWithDifferentParameterNames());
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(matcher, candidates));
    }

    @Test
    void shouldFailOnDuplicateCandidatesAcrossSeveralClassesAndParameters() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new GivenStepOne());
        candidates.add(new GivenStepTwo());
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(matcher, candidates));
    }

    @Test
    void shouldFailOnDuplicateCandidatesInClassAndCompositee() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new GivenStepOne());
        CandidateSteps compositeSteps = new CompositeCandidateSteps(new MostUsefulConfiguration(),
                Collections.singleton("composite.steps"));
        candidates.add(compositeSteps);
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(matcher, candidates));
    }

    @Test
    void shouldGetConditionalStepsAcrossSeveralClassesIfStepsAnnotatedWithCondition() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new ClassWithOneConditionalStep1());
        candidates.add(new ClassWithOneConditionalStep2());
        candidates.add(new GivenStepOne());
        List<StepCandidate> steps = new AllStepCandidates(matcher, candidates).getRegularSteps();
        assertThat(steps, hasSize(2));
        Collections.sort(steps, Comparator.comparing(StepCandidate::getName));
        assertStepCandidate(steps.get(0), StepCandidate.class, StepType.GIVEN,
                "$customer has previously bought a $product");
        assertStepCandidate(steps.get(1), ConditionalStepCandidate.class, StepType.GIVEN, CONDITIONAL_STEP);
    }

    @Test
    void shouldGetConditionalStepFromClassWithOneAnnotatedStep() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new ClassWithOneConditionalStep1());
        List<StepCandidate> steps = new AllStepCandidates(matcher, candidates).getRegularSteps();
        assertThat(steps, hasSize(1));
        assertStepCandidate(steps.get(0), ConditionalStepCandidate.class, StepType.GIVEN, CONDITIONAL_STEP);
    }

    @Test
    void shouldGetConditionalStepsFromClassAnnotatedWithCondition() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new ClassWithConditionAtClassLevel());
        List<StepCandidate> steps = new AllStepCandidates(matcher, candidates).getRegularSteps();
        assertThat(steps, hasSize(1));
        assertStepCandidate(steps.get(0), ConditionalStepCandidate.class, StepType.GIVEN, CONDITIONAL_STEP);
    }

    @Test
    void shouldNotFailWithDuplicateCandidateFoundExceptionIfStepsWordingsDoNotMatchEachOther() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new StepsWithParameters());
        List<StepCandidate> steps = new AllStepCandidates(matcher, candidates).getRegularSteps();
        steps.sort(Comparator.comparing(StepCandidate::getPatternAsString));
        assertThat(steps, hasSize(2));
        assertStepCandidate(steps.get(0), StepCandidate.class, StepType.GIVEN,
                "a given param '$givenParameter' and '$secondParam'");
        assertStepCandidate(steps.get(1), StepCandidate.class, StepType.GIVEN,
                "a given param '$someParameterName'");
    }

    @Test
    void shouldCollectStepWithItsAliasInCorrectOrder() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new StepWithAlias());
        List<StepCandidate> steps = new AllStepCandidates(matcher, candidates).getRegularSteps();
        assertThat(steps, hasSize(2));
        assertStepCandidate(steps.get(0), StepCandidate.class, StepType.GIVEN, "step with Alias");
        assertStepCandidate(steps.get(1), StepCandidate.class, StepType.GIVEN, "Alias step");
    }

    private static void assertStepCandidate(StepCandidate candidate, java.lang.Class<?> clazz, StepType type,
            String name) {
        assertThat(candidate, instanceOf(clazz));
        assertEquals(type, candidate.getStepType());
        assertEquals(name, candidate.getPatternAsString());
    }

    static class TestCondition implements Predicate<Object> {

        @Override
        public boolean test(Object t) {
            return true;
        }

    }

    static class StepsWithParameters extends Steps {

        @Given("a given param '$someParameterName'")
        public void given(String someParameterName) {
        }

        @Given("a given param '$givenParameter' and '$secondParam'")
        public void duplicateGiven(String givenParameter, String secondParam) {
        }
    }

    static class StepWithAlias extends Steps {

        @Given("step with Alias")
        @Alias("Alias step")
        public void given() {
        }
    }

    static class ClassWithOneConditionalStep1 extends Steps {

        @Conditional(condition = TestCondition.class, value = "step1")
        @Given(CONDITIONAL_STEP)
        public void givenConditionalStep1() {
        }

    }

    static class ClassWithOneConditionalStep2 extends Steps {

        @Conditional(condition = TestCondition.class, value = "step2")
        @Given(CONDITIONAL_STEP)
        public void givenConditionalStep2() {
        }

    }

    @Conditional(condition = TestCondition.class, value = "class")
    static class ClassWithConditionAtClassLevel extends Steps {

        @Given(CONDITIONAL_STEP)
        public void givenConditionalStep1() {
        }

        @Given(CONDITIONAL_STEP)
        public void givenConditionalStep2() {
        }

    }

    static class DuplicateStepsInOneClass extends Steps {

        @Given("a given")
        public void given() {
        }

        @Given("a given")
        public void duplicateGiven() {
        }

    }

    static class DuplicateStepsWithDifferentParameterNames extends Steps {

        @Given("a given $aaa step")
        public void given(String value) {
        }

        @Given("a given $bbb step")
        public void duplicateGiven(String value) {
        }

    }

    static class GivenStepOne extends Steps {

        @Given("$customer has previously bought a $product")
        public void given(String customer, String product) {
        }

    }

    static class GivenStepTwo extends Steps {

        @Given("$customer has previously bought a $product")
        public void given(String customer, String product) {
        }

    }
}
