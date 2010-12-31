package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.When;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.Ignore;
import org.junit.Test;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompositeStepCandidateBehaviour {

    @Test
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingMatchedParameters() {
        CompositeSteps steps = new CompositeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<String, String>();
        List<Step> composedSteps = candidate.createComposedSteps("Given Mr Jones has previously bought a ticket",
                noNamedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform();
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class CompositeSteps extends Steps {

        private String loggedIn;
        private String added;

        @Given("$customer has previously bought a $product")
        @Composite(steps = { "Given <customer> is logged in", "When a <product> is added to the cart" })
        public void aCompositeStep(@Named("customer") String customer, @Named("product") String product) {
        }

        @Given("<customer> is logged in")
        public void aCustomerIsLoggedIn(@Named("customer") String customer) {
            loggedIn = customer;
        }

        @When("a <product> is added to the cart")
        public void aProductIsAddedToCart(@Named("product") String product) {
            added = product;
        }

    }

    @Test
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingNamedParameters() {
        CompositeStepsUsingNamedParameters steps = new CompositeStepsUsingNamedParameters();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> namedParameters = new HashMap<String, String>();
        namedParameters.put("customer", "Mr Jones");
        namedParameters.put("product", "ticket");
        List<Step> composedSteps = candidate.createComposedSteps("Given <customer> has previously bought a <product>",
                namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform();
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class CompositeStepsUsingNamedParameters extends Steps {

        private String loggedIn;
        private String added;

        @Given("<customer> has previously bough a <product>")
        @Composite(steps = { "Given <customer> is logged in", "When a <product> is added to the cart" })
        public void aCompositeStep(@Named("customer") String customer, @Named("product") String product) {
        }

        @Given("<customer> is logged in")
        public void aCustomerIsLoggedIn(@Named("customer") String customer) {
            loggedIn = customer;
        }

        @When("a <product> is added to the cart")
        public void aProductIsAddedToCart(@Named("product") String product) {
            added = product;
        }

    }

    @Test
    @Ignore("fails as perhaps Paranamer not peer of @named in respect of @composite")
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingParanamerNamedParameters() {
        CompositeStepsWithoutNamedAnnotation steps = new CompositeStepsWithoutNamedAnnotation();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        candidate.useParanamer(new BytecodeReadingParanamer());
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> namedParameters = new HashMap<String, String>();
        namedParameters.put("customer", "Mr Jones");
        namedParameters.put("product", "ticket");
        List<Step> composedSteps = candidate.createComposedSteps("Given <customer> has previously bought a <product>",
                namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform();
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class CompositeStepsWithoutNamedAnnotation extends Steps {

        private String loggedIn;
        private String added;

        @Given("<customer> has previously bough a <product>")
        @Composite(steps = { "Given <customer> is logged in", "When a <product> is added to the cart" })
        public void aCompositeStep(String customer, String product) {
        }

        @Given("<customer> is logged in")
        public void aCustomerIsLoggedIn(String customer) {
            loggedIn = customer;
        }

        @When("a <product> is added to the cart")
        public void aProductIsAddedToCart(String product) {
            added = product;
        }

    }

    @Test
    public void shouldNoteThatOneOfTheStepsIsPendingSoThatRemainingStepsAreNotRun() {
        CompositeStepComposedOfAnImplementedAndAPendingStep steps = new CompositeStepComposedOfAnImplementedAndAPendingStep();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<String, String>();
        List<Step> composedSteps = candidate.createComposedSteps("Given I have done stuff", noNamedParameters,
                candidates);
        assertThat(composedSteps.size(), equalTo(2));
        StoryReporter storyReporter = mock(StoryReporter.class);
        for (Step composedStep : composedSteps) {
            composedStep.perform().describeTo(storyReporter);
        }
        verify(storyReporter).successful("Given I did stuff");
        verify(storyReporter).pending("When I did additional stuff");
    }

    static class CompositeStepComposedOfAnImplementedAndAPendingStep extends Steps {

        @Given("I have done stuff")
        @Composite(steps = { "Given I did stuff", "When I did additional stuff" })
        public void aCompositeStep() {
        }

        @Given("I did stuff")
        public void didStuff() {
        }
    }

}
