package org.jbehave.core.steps;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompositeStepCandidateBehaviour {

    @Test
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingMatchedParameters() {
        CompositeSteps steps = new CompositeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<String, String>();
        List<Step> composedSteps = new ArrayList<Step>();
        candidate.addComposedSteps(composedSteps, "Given Mr Jones has previously bought a ticket", noNamedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform(null);
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class CompositeSteps extends Steps {

        private String loggedIn;
        private String added;

        @Given("$customer has previously bought a $product")
        @Composite(steps = { "Given <customer> is logged in",
                             "When a <product> is added to the cart" })
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
        List<Step> composedSteps = new ArrayList<Step>();
        candidate.addComposedSteps(composedSteps, "Given <customer> has previously bought a <product>", namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform(null);
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class CompositeStepsUsingNamedParameters extends Steps {

        private String loggedIn;
        private String added;

        @Given("<customer> has previously bough a <product>")
        @Composite(steps = { "Given <customer> is logged in",
                             "When a <product> is added to the cart" })
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
        List<Step> composedSteps = new ArrayList<Step>();
        candidate.addComposedSteps(composedSteps, "Given <customer> has previously bought a <product>", namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform(null);
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class CompositeStepsWithoutNamedAnnotation extends Steps {

        private String loggedIn;
        private String added;

        @Given("<customer> has previously bough a <product>")
        @Composite(steps = {"Given <customer> is logged in",
                "When a <product> is added to the cart"})
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
    public void shouldMatchCompositeStepsAndCreateComposedNestedSteps() {
        CompositeNestedSteps steps = new CompositeNestedSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<String, String>();
        List<Step> composedSteps = new ArrayList<Step>();
        candidate.addComposedSteps(composedSteps, "Then all buttons are enabled", noNamedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(6));
        for (Step step : composedSteps) {
            step.perform(null);
        }
        assertThat(steps.trail.toString(), equalTo("l>l1>l2>t>t1>t2>"));
    }

    static class CompositeNestedSteps extends Steps {

        private StringBuffer trail = new StringBuffer();
        
        @Then("all buttons are enabled")
        @Composite(steps = {
            "Then all left buttons are enabled",
            "Then all top buttons are enabled" }
        )
        public void all() {
            trail.append("a>");
        }

        @Then("all left buttons are enabled")
        @Composite(steps = {
            "Then first left button is enabled",
            "Then second left button is enabled" }
        )
        public void leftAll() {
            trail.append("l>");
        }

        @Then("first left button is enabled")
        public void leftOne(){
            trail.append("l1>");
        }

        @Then("second left button is enabled")
        public void leftTwo(){
            trail.append("l2>");
        }

        @Then("all top buttons are enabled")
        @Composite(steps = {
            "Then first top button is enabled",
            "Then second top button is enabled" }
        )
        public void topAll() {
            trail.append("t>");
        }

        @Then("first top button is enabled")
        public void topOne() {
            trail.append("t1>");
        }

        @Then("second top button is enabled")
        public void topTwo() {
            trail.append("t2>");
        }

    }

}
