package org.jbehave.core.steps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.CandidateStep.StartingWordNotFound;
import org.jbehave.core.steps.Steps.DuplicateCandidateStepFoundException;
import org.junit.Test;

public class StepsBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldListCandidateStepsFromAnnotatedMethodsWithSingleAlias() {
        SingleAliasSteps steps = new SingleAliasSteps();
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(6));

        findCandidateStep(candidateSteps, "GIVEN a given").createMatchedStep("Given a given", tableRow).perform();
        findCandidateStep(candidateSteps, "GIVEN a given alias").createMatchedStep("Given a given alias", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN a when").createMatchedStep("When a when", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN a when alias").createMatchedStep("When a when alias", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN a then").createMatchedStep("Then a then", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN a then alias").createMatchedStep("Then a then alias", tableRow).perform();
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

	@Test
    public void shouldListCandidateStepsFromAnnotatedMethodsWithMultipleAliases() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(9));
        
        findCandidateStep(candidateSteps, "GIVEN a given").createMatchedStep("Given a given", tableRow).perform();
        findCandidateStep(candidateSteps, "GIVEN a given alias").createMatchedStep("Given a given alias", tableRow).perform();
        findCandidateStep(candidateSteps, "GIVEN another given alias").createMatchedStep("Given another given alias", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN a when").createMatchedStep("When a when", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN a when alias").createMatchedStep("When a when alias", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN another when alias").createMatchedStep("When another when alias", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN a then").createMatchedStep("Then a then", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN a then alias").createMatchedStep("Then a then alias", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN another then alias").createMatchedStep("Then another then alias", tableRow).perform();

        assertThat(steps.givens, equalTo(3));
        assertThat(steps.whens, equalTo(3));
        assertThat(steps.thens, equalTo(3));
    }

    @Test
    public void shouldListCandidateStepsFromAnnotatedMethodsInPojo() {
        PojoSteps steps = new PojoSteps();
        Configuration configuration = new MostUsefulConfiguration();
		List<CandidateStep> candidateSteps = new InstanceStepsFactory(configuration, steps).createCandidateSteps().get(0).listCandidates();
        assertThat(candidateSteps.size(), equalTo(6));

        findCandidateStep(candidateSteps, "GIVEN a given").createMatchedStep("Given a given", tableRow).perform();
        findCandidateStep(candidateSteps, "GIVEN a given alias").createMatchedStep("Given a given alias", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN a when").createMatchedStep("When a when", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN a when alias").createMatchedStep("When a when alias", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN a then").createMatchedStep("Then a then", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN a then alias").createMatchedStep("Then a then alias", tableRow).perform();
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

    private CandidateStep findCandidateStep(List<CandidateStep> candidateSteps, String candidateStepAsString) {
        for (CandidateStep candidateStep : candidateSteps) {
            if ( candidateStepAsString.equals(candidateStep.toString()) ){
                return candidateStep;
            }
        }
        throw new RuntimeException("CandidateStep "+candidateStepAsString+" not found amongst "+candidateSteps);
    }

    @Test
    public void shouldProvideStepsToBePerformedBeforeStory() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<Step> beforeStory = steps.runBeforeStory(false);
        assertThat(beforeStory.size(), equalTo(1));        
        beforeStory.get(0).perform();
        assertThat(steps.beforeStory, is(true));
        
        List<Step> beforeGivenStory = steps.runBeforeStory(true);
        assertThat(beforeGivenStory.size(), equalTo(1));        
        beforeGivenStory.get(0).perform();
        assertThat(steps.beforeGivenStory, is(true));
    }
    
    @Test
    public void shouldProvideStepsToBePerformedAfterStory() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<Step> afterStory = steps.runAfterStory(false);
        assertThat(afterStory.size(), equalTo(1));        
        afterStory.get(0).perform();
        assertThat(steps.afterStory, is(true));
        
        List<Step> afterGivenStory = steps.runAfterStory(true);
        assertThat(afterGivenStory.size(), equalTo(1));        
        afterGivenStory.get(0).perform();
        assertThat(steps.afterGivenStory, is(true));
    }

    
    @Test
    public void shouldProvideStepsToBePerformedBeforeScenarios() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
    	List<Step> executableSteps = steps.runBeforeScenario();
		assertThat(executableSteps.size(), equalTo(1));
		
    	executableSteps.get(0).perform();
    	assertThat(steps.beforeScenario, is(true));
    }
    
    @Test
    public void shouldProvideStepsToBePerformedAfterScenarioUponOutcome() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
    	List<Step> executableSteps = steps.runAfterScenario();
    	assertThat(executableSteps.size(), equalTo(3));
    	
    	// uponOutcome=ANY
    	executableSteps.get(0).perform();
    	assertThat(steps.afterAnyScenario, is(true));
    	
    	// uponOutcome=SUCCESS
    	executableSteps.get(1).perform();
    	assertThat(steps.afterSuccessfulScenario, is(true));
    	
		// uponOutcome=FAILURE
    	executableSteps.get(2).perform();
    	assertThat(steps.afterFailedScenario, is(false));
    }
    
    @Test
    public void shouldProvideStepsToBeNotPerformedAfterScenarioUponOutcome() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
    	List<Step> executableSteps = steps.runAfterScenario();
    	
    	// uponOutcome=ANY
    	executableSteps.get(0).doNotPerform();
    	assertThat(steps.afterAnyScenario, is(true)); 
    	
    	// uponOutcome=SUCCESS
		executableSteps.get(1).doNotPerform();
		assertThat(steps.afterSuccessfulScenario, is(false)); 
				
		// uponOutcome=FAILURE
		executableSteps.get(2).doNotPerform();
		assertThat(steps.afterFailedScenario, is(true)); 
		
    }
    
    @Test
    public void shouldAllowLocalizationOfSteps(){
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
    	LocalizedSteps steps = new LocalizedSteps(configuration);
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(3));

        findCandidateStep(candidateSteps, "GIVEN un dato che").createMatchedStep("Dato che un dato che", tableRow).perform();
        findCandidateStep(candidateSteps, "WHEN un quando").createMatchedStep("Quando un quando", tableRow).perform();
        findCandidateStep(candidateSteps, "THEN un allora").createMatchedStep("Allora un allora", tableRow).perform();

        assertThat(steps.givens, equalTo(1));
        assertThat(steps.whens, equalTo(1));
        assertThat(steps.thens, equalTo(1));    	    	
    }

    @Test(expected=BeforeOrAfterFailed.class)
    public void shouldReportFailuresInBeforeAndAfterMethods() {
    	BeforeAndAfterSteps steps = new BeforeAndAfterSteps();
    	List<Step> executableSteps = steps.runBeforeScenario();
    	executableSteps.get(0).perform();
    	executableSteps.get(1).perform();
    }

    @Test(expected=DuplicateCandidateStepFoundException.class)
    public void shouldFailIfDuplicateStepsAreEncountered() {
        DuplicateSteps steps = new DuplicateSteps();
        List<CandidateStep> candidateSteps = steps.listCandidates();

        assertThat(candidateSteps.size(), equalTo(2));
        candidateSteps.get(0).createMatchedStep("Given a given", tableRow).perform();

    }

    @Test(expected=StartingWordNotFound.class)
    public void shouldNotCreateStepIfStartingWordNotFound(){
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
    	LocalizedSteps steps = new LocalizedSteps(configuration);
        List<CandidateStep> candidateSteps = steps.listCandidates();
        assertThat(candidateSteps.size(), equalTo(3));

        // misspelled starting word 
        candidateSteps.get(0).createMatchedStep("Dado che un dato che", tableRow); 
        
    }
    
    static class MultipleAliasesSteps extends Steps {
        
        private int givens;
        private int whens;
        private int thens;
        
        private boolean beforeScenario;
        private boolean afterAnyScenario;
        private boolean afterSuccessfulScenario;
        private boolean afterFailedScenario;
        private boolean beforeStory;
        private boolean afterStory;
        private boolean beforeGivenStory;
        private boolean afterGivenStory;
        
        @org.jbehave.core.annotations.Given("a given")
        @org.jbehave.core.annotations.Aliases(values={"a given alias", "another given alias"})
        public void given() {
            givens++;
        }

        @org.jbehave.core.annotations.When("a when")
        @org.jbehave.core.annotations.Aliases(values={"a when alias", "another when alias"})
        public void when() {
            whens++;
        }
        
        @org.jbehave.core.annotations.Then("a then")
        @org.jbehave.core.annotations.Aliases(values={"a then alias", "another then alias"})
        public void then() {
            thens++;
        }

        @org.jbehave.core.annotations.BeforeStory
        public void beforeStory() {
            beforeStory = true;
        }
        
        @org.jbehave.core.annotations.AfterStory
        public void afterStory() {
            afterStory = true;
        }

        @org.jbehave.core.annotations.BeforeStory(uponGivenStory=true)
        public void beforeGivenStory() {
            beforeGivenStory = true;
        }
        
        @org.jbehave.core.annotations.AfterStory(uponGivenStory=true)
        public void afterGivenStory() {
            afterGivenStory = true;
        }        
        
        @org.jbehave.core.annotations.BeforeScenario
        public void beforeScenarios() {
        	beforeScenario = true;
        }
        
        @org.jbehave.core.annotations.AfterScenario
        public void afterAnyScenarios() {
        	afterAnyScenario = true;
        }
        
        @org.jbehave.core.annotations.AfterScenario(uponOutcome=AfterScenario.Outcome.SUCCESS)
        public void afterSuccessfulScenarios() {
        	afterSuccessfulScenario = true;
        }
        
        @org.jbehave.core.annotations.AfterScenario(uponOutcome=AfterScenario.Outcome.FAILURE)
        public void afterFailedScenarios() {
        	afterFailedScenario = true;
        }
        
        
    }

    static class SingleAliasSteps extends Steps {

        private int givens;
        private int whens;
        private int thens;

        @org.jbehave.core.annotations.Given("a given")
        @org.jbehave.core.annotations.Alias("a given alias")
        public void given() {
            givens++;
        }

        @org.jbehave.core.annotations.When("a when")
        @org.jbehave.core.annotations.Alias("a when alias")
        public void when() {
            whens++;
        }

        @org.jbehave.core.annotations.Then("a then")
        @org.jbehave.core.annotations.Alias("a then alias")
        public void then() {
            thens++;
        }

    }

    static class PojoSteps {

        private int givens;
        private int whens;
        private int thens;

        @org.jbehave.core.annotations.Given("a given")
        @org.jbehave.core.annotations.Alias("a given alias")
        public void given() {
            givens++;
        }

        @org.jbehave.core.annotations.When("a when")
        @org.jbehave.core.annotations.Alias("a when alias")
        public void when() {
            whens++;
        }

        @org.jbehave.core.annotations.Then("a then")
        @org.jbehave.core.annotations.Alias("a then alias")
        public void then() {
            thens++;
        }

    }

    static class BeforeAndAfterSteps extends Steps {
        
        @org.jbehave.core.annotations.BeforeScenario
        public void beforeScenario() {
        }

        @org.jbehave.core.annotations.BeforeScenario
        public void beforeScenarioThatFails() {
        	throw new RuntimeException("Damn, I failed!");
        }
                
    }

    static class DuplicateSteps extends Steps {
        
        @org.jbehave.core.annotations.Given("a given")
        public void given() {
        }

        @org.jbehave.core.annotations.Given("a given")
        public void duplicateGiven() {
        }
                
    }

    static class LocalizedSteps extends Steps {

        private int givens;
        private int whens;
        private int thens;

        public LocalizedSteps(Configuration configuration) {
        	super(configuration);
		}

		@org.jbehave.core.annotations.Given("un dato che")
        public void given() {
            givens++;
        }

        @org.jbehave.core.annotations.When("un quando")
        public void when() {
            whens++;
        }

        @org.jbehave.core.annotations.Then("un allora")
        public void then() {
            thens++;
        }

    }
}
