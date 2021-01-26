package org.jbehave.core.steps;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import org.jbehave.core.annotations.*;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jbehave.core.steps.StepCandidateBehaviour.candidateMatchingStep;
import static org.mockito.Mockito.mock;

public class CompositeCandidateStepsBehaviour {

    @Test
    public void shouldMatchCompositesAndCreateComposedStepsUsingMatchedParameters() {
        CandidateSteps compositeSteps = new SimpleCompositeSteps();
        shouldMatchCompositesAndCreateComposedStepsUsingMatchedParameters(compositeSteps);
    }

    @Test
    public void shouldMatchCompositesFromFileAndCreateComposedStepsUsingMatchedParameters() {
        CandidateSteps compositeSteps = new CompositeCandidateSteps(new MostUsefulConfiguration(),
                Collections.singleton("composite.steps"));
        shouldMatchCompositesAndCreateComposedStepsUsingMatchedParameters(compositeSteps);
    }

    private void shouldMatchCompositesAndCreateComposedStepsUsingMatchedParameters(CandidateSteps candidateSteps) {
        SimpleSteps steps = new SimpleSteps();
        List<StepCandidate> candidates = new ArrayList<>();
        candidates.addAll(steps.listCandidates());
        candidates.addAll(candidateSteps.listCandidates());
        StepCandidate candidate = candidateMatchingStep(candidates, "Given $customer has previously bought a $product");
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> namedParameters = new HashMap<>();
        namedParameters.put("customer", "Ms Smith");
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, "Given Mr Jones has previously bought a ticket", namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform(mock(StoryReporter.class), null);
        }
        assertThat(steps.loggedIn, equalTo("Mr Jones"));
        assertThat(steps.added, equalTo("ticket"));
    }

    static class SimpleSteps extends Steps {

        private String loggedIn;
        private String added;

        @Given("<customer> is logged in")
        public void aCustomerIsLoggedIn(@Named("customer") String customer) {
            loggedIn = customer;
        }

        @When("a <product> is added to the cart")
        public void aProductIsAddedToCart(@Named("product") String product) {
            added = product;
        }

    }

    static class SimpleCompositeSteps extends Steps {

        @Given("$customer has previously bought a $product")
        @Composite(steps = { "Given <customer> is logged in",
                "When a <product> is added to the cart" })
        public void aCompositeStep(@Named("customer") String customer, @Named("product") String product) {
        }

    }


    @Test
    public void shouldMatchCompositesAndCreateComposedStepsUsingNamedParameters() {
        CompositeStepsUsingNamedParameters steps = new CompositeStepsUsingNamedParameters();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidateMatchingStep(candidates, "Given <customer> has previously bough a <product>");
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> namedParameters = new HashMap<>();
        namedParameters.put("customer", "Mr Jones");
        namedParameters.put("product", "ticket");
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, "Given <customer> has previously bought a <product>", namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform(mock(StoryReporter.class), null);
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
    @Disabled("fails as perhaps Paranamer not peer of @named in respect of @composite")
    public void shouldMatchCompositesAndCreateComposedStepsUsingParanamerNamedParameters() {
        CompositeStepsWithoutNamedAnnotation steps = new CompositeStepsWithoutNamedAnnotation();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidates.get(0);
        candidate.useParanamer(new BytecodeReadingParanamer());
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> namedParameters = new HashMap<>();
        namedParameters.put("customer", "Mr Jones");
        namedParameters.put("product", "ticket");
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, "Given <customer> has previously bought a <product>", namedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            step.perform(mock(StoryReporter.class), null);
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
    public void shouldMatchCompositesAndCreateComposedNestedSteps() {
        NestedCompositeSteps steps = new NestedCompositeSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        // find main step
        StepCandidate candidate = null;
        for(StepCandidate cand: candidates) {
            if(cand.getPatternAsString().equals("all buttons are enabled")) {
                candidate = cand;
                break;
            }
        }
        assertThat(candidate, is(notNullValue()));
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<>();
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, "Then all buttons are enabled", noNamedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(2));
        for (Step step : composedSteps) {
            StoryReporter storyReporter = mock(StoryReporter.class);
            step.perform(storyReporter, null);
            List<Step> nestedComposedSteps = step.getComposedSteps();
            assertThat(nestedComposedSteps.size(), equalTo(2));
            for (Step nestedComposedStep : nestedComposedSteps) {
                nestedComposedStep.perform(storyReporter, null);
            }
        }
        assertThat(steps.trail.toString(), equalTo("left>left_first>left_second>top>top_first>top_second>"));
    }

    static class NestedCompositeSteps extends Steps {

        private final StringBuilder trail = new StringBuilder();

        @When("all buttons are enabled")
        @Composite(steps = {
            "Then all left buttons are enabled",
            "Then all top buttons are enabled" }
        )
        public void all() {
            trail.append("a>");
        }

        @Then("all $location buttons are enabled")
        @Composite(steps = {
            "Then first <location> button is enabled",
            "Then second <location> button is enabled" }
        )
        public void topAll(String location) {
            trail.append(location).append('>');
        }

        @Then("$order $location button is enabled")
        public void topOne(String order, String location) {
            trail.append(location).append('_').append(order).append('>');
        }
    }

    @Test
    public void shouldMatchCompositesWhenStepParameterIsProvided(){
        CompositeStepsWithParameterMatching steps = new CompositeStepsWithParameterMatching();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidateMatchingStep(candidates, "When I login");
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<>();
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, "When I login", noNamedParameters, candidates);
        assertThat(composedSteps.size(), equalTo(1));
        for (Step step : composedSteps) {
            step.perform(mock(StoryReporter.class), null);
        }
        assertThat(steps.button, equalTo("Login"));
    }
    
    static class CompositeStepsWithParameterMatching extends Steps {
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
    public void recursiveCompositesShouldWorkWithSomeMissingParameters(){
        String userName = "someUserName";
        CompositeStepsWithParameterMissing steps = new CompositeStepsWithParameterMissing();
        List<StepCandidate> candidates = steps.listCandidates();
        StepCandidate candidate = candidateMatchingStep(candidates, "Given I am logged in as " + userName);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<>();
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, "Given I am logged in as someUserName", noNamedParameters, candidates);
        for (Step step : composedSteps) {
            step.perform(mock(StoryReporter.class), null);
        }
        assertThat("Was unable to set the username", steps.username, equalTo(userName));
        assertThat("Didn't reach the login step", steps.isLoggedIn, is(true));
    }
    
    static class CompositeStepsWithParameterMissing extends Steps {
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

    @Test
    public void shouldIgnoreCompositesIgnorableStep(){
        CompositeWithIgnorableStep steps = new CompositeWithIgnorableStep();
        List<StepCandidate> candidates = steps.listCandidates();
        String compositeName = "When ignore my step";
        StepCandidate candidate = candidateMatchingStep(candidates, compositeName);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<>();
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, compositeName, noNamedParameters, candidates);
        StoryReporter reporter = mock(StoryReporter.class);
        for (Step step : composedSteps) {
            StepResult result = step.perform(reporter, null);
            result.describeTo(reporter);
        }
        Mockito.verify(reporter).ignorable("!-- When ignore me");
    }

    static class CompositeWithIgnorableStep extends Steps {

        @When("ignore my step")
        @Composite(steps={"!-- When ignore me"})
        public void whenIgnoreMyStep(){}

        @When("ignore me")
        public void whenIgnoreMe(){}
    }

    @Test
    public void shouldCommentCompositesComment(){
        CompositeWithComment steps = new CompositeWithComment();
        List<StepCandidate> candidates = steps.listCandidates();
        String compositeName = "When comment my comment";
        StepCandidate candidate = candidateMatchingStep(candidates, compositeName);
        assertThat(candidate.isComposite(), is(true));
        Map<String, String> noNamedParameters = new HashMap<>();
        List<Step> composedSteps = new ArrayList<>();
        candidate.addComposedSteps(composedSteps, compositeName, noNamedParameters, candidates);
        StoryReporter reporter = mock(StoryReporter.class);
        for (Step step : composedSteps) {
            StepResult result = step.perform(reporter, null);
            result.describeTo(reporter);
        }
        Mockito.verify(reporter).comment("!-- comment");
    }

    static class CompositeWithComment extends Steps {

        @When("comment my comment")
        @Composite(steps={"!-- comment"})
        public void whenIgnoreMyStep(){}
    }
}
