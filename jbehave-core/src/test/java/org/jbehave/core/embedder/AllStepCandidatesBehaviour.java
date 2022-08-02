package org.jbehave.core.embedder;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.CompositeCandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;

class AllStepCandidatesBehaviour {

    @Test
    void shouldFailOnDuplicateCandidatesInOneClass() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new DuplicateStepsInOneClass());
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(candidates));
    }

    @Test
    void shouldFailOnDuplicateCandidatesAcrossSeveralClassesAndParameters() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new GivenStepOne());
        candidates.add(new GivenStepTwo());
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(candidates));
    }

    @Test
    void shouldFailOnDuplicateCandidatesInClassAndCompositee() {
        List<CandidateSteps> candidates = new ArrayList<>();
        candidates.add(new GivenStepOne());
        CandidateSteps compositeSteps = new CompositeCandidateSteps(new MostUsefulConfiguration(),
                Collections.singleton("composite.steps"));
        candidates.add(compositeSteps);
        assertThrows(DuplicateCandidateFound.class, () -> new AllStepCandidates(candidates));
    }

    static class DuplicateStepsInOneClass extends Steps {

        @Given("a given")
        public void given() {
        }

        @Given("a given")
        public void duplicateGiven() {
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
