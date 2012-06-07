package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.Ignore;
import org.junit.Test;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.jbehave.core.steps.StepCandidateBehaviour.candidateMatchingStep;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CompositeStepCandidateBehaviour {

    @Test
    public void shouldMatchCompositeStepsAndCreateComposedStepsUsingMatchedParameters() {
        CompositeSteps steps = new CompositeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidateMatchingStep(candidates, "Given $customer has previously bought a $product");
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
        StepCandidate candidate = candidateMatchingStep(candidates, "Given <customer> has previously bough a <product>");
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
        // find main step
        StepCandidate candidate = null;
        for(StepCandidate cand: candidates) {
            if(cand.getPatternAsString().equals("all buttons are enabled")) {
                candidate = cand;
                break;
            }
        }
        assertNotNull(candidate);
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

    @Test
    public void shouldMatchCompositeStepsWhenStepParameterIsProvided(){
        CompositeStepsParameterMatching steps = new CompositeStepsParameterMatching();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidateMatchingStep(candidates, "When I login");
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<String, String>();
        List<Step> composedSteps = new ArrayList<Step>();
        candidate.addComposedSteps(composedSteps, "When I login", noNamedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(1));
        for (Step step : composedSteps) {
            step.perform(null);
        }
        assertThat(steps.button, equalTo("Login"));
    }
    
    static class CompositeStepsParameterMatching extends Steps {
        private String button;
        

        @When("I login")
        @Composite(steps={"When I click the Login button"})
        public void whenILogin(){}
        
        @When("I click the $button button")
        public void whenIClickTheButton(@Named("button") String button){
            this.button = button;
        }
        
    }
    
    @Test
    public void recursiveCompositeStepsShouldWorkWithSomeMissingParameters(){
        String userName = "someUserName";
        CompositeStepParametersWithMissingParameters steps = new CompositeStepParametersWithMissingParameters();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidateMatchingStep(candidates, "Given I am logged in as " + userName);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<String, String>();
        List<Step> composedSteps = new ArrayList<Step>();
        candidate.addComposedSteps(composedSteps, "Given I am logged in as someUserName", noNamedParameters, candidates);
        for (Step step : composedSteps) {
            step.perform(null);
        }
        assertThat("Was unable to set the username", steps.username, equalTo(userName));
        assertTrue("Didn't reach the login step", steps.isLoggedIn);
    }
    
    static class CompositeStepParametersWithMissingParameters extends Steps {
        private String username;
        private boolean isLoggedIn;
        
        
        @Given("I am logged in as $name")
        @Composite(steps = {
                "Given my user name is <name>",
                "Given I log in"
        })
        public void logInAsUser(@Named("name")String name){}
        
        @Given("my user name is $name")
        public void setUsername(@Named("name")String name){
            this.username = name;
        }
        
        @Given("I log in")
        @Composite(steps = {
                "Given I am on the Login page", 
                "When I type my user name into the Username field", 
                "When I type my password into the Password field", 
                "When I click the Login button"} )
        public void logIn(){
            this.isLoggedIn = true;
        }
        
        @Given("Given I am on the Login page")
        public void onLoginPage(){}
        
        @Given("When I type my user name into the Username field")
        public void typeUsername(){}
        
        @Given("When I type my password into the Password field")
        public void typePassword(){}
        
        @Given("When I click the Login button")
        public void clickLogin(){}
        
        
    }

}
