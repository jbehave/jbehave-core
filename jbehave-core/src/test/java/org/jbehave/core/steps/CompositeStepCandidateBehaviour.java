package org.jbehave.core.steps;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.When;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompositeStepCandidateBehaviour {

    private Map<String, String> namedParameters = new HashMap<String, String>();

    @Test
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingMatchedParameters() {
        CompositeSteps steps = new CompositeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(4));
        StepCandidate candidate = candidates.get(0);
        candidate.useParanamer(new BytecodeReadingParanamer());
        assertThat(candidate.isComposite(), is(true));
        List<Step> composedSteps = candidate.createComposedSteps("Given Mr Jones has previously bought a ticket", namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform();
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    @Test
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingNamedParameters() {
        CompositeSteps steps = new CompositeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(4));
        StepCandidate candidate = candidates.get(0);
        candidate.useParanamer(new BytecodeReadingParanamer());
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> namedParameters = new HashMap<String, String>();
        namedParameters.put("customer", "Mr Jones");
        namedParameters.put("product", "ticket");
        List<Step> composedSteps = candidate.createComposedSteps("Given <customer> has previously bought a <product>", namedParameters, candidates);
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

        @Given("$customer has previously bought a $product") // used with matched parameters
        @Alias("<customer> has previously bough a <product>") // used with named parameters
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


}
