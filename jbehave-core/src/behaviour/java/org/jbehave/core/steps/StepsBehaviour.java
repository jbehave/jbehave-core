package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.CandidateStep.StartingWordNotFound;
import org.jbehave.core.steps.Steps.DuplicateCandidateStepFoundException;
import org.junit.Test;

public class StepsBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

	@Test
    public void shouldProvideCandidateStepsCorrespondingToAnnotatedStepsWithMultipleAliases() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        CandidateStep[] candidateSteps = steps.getSteps();
        assertThat(candidateSteps.length, equalTo(9));
        
        findCandidateStep(candidateSteps, "GIVEN a given").createFrom(tableRow, "Given a given").perform();
        findCandidateStep(candidateSteps, "GIVEN a given alias").createFrom(tableRow, "Given a given alias").perform();
        findCandidateStep(candidateSteps, "GIVEN another given alias").createFrom(tableRow, "Given another given alias").perform();
        findCandidateStep(candidateSteps, "WHEN a when").createFrom(tableRow, "When a when").perform();
        findCandidateStep(candidateSteps, "WHEN a when alias").createFrom(tableRow, "When a when alias").perform();
        findCandidateStep(candidateSteps, "WHEN another when alias").createFrom(tableRow, "When another when alias").perform();
        findCandidateStep(candidateSteps, "THEN a then").createFrom(tableRow, "Then a then").perform();
        findCandidateStep(candidateSteps, "THEN a then alias").createFrom(tableRow, "Then a then alias").perform();
        findCandidateStep(candidateSteps, "THEN another then alias").createFrom(tableRow, "Then another then alias").perform();

        assertThat(steps.givens, equalTo(3));
        assertThat(steps.whens, equalTo(3));
        assertThat(steps.thens, equalTo(3));
    }

    @Test
    public void shouldProvideCandidateStepsCorrespondingToAnnotatedStepsWithSingleAlias() {
        SingleAliasSteps steps = new SingleAliasSteps();
        CandidateStep[] candidateSteps = steps.getSteps();
        assertThat(candidateSteps.length, equalTo(6));

        findCandidateStep(candidateSteps, "GIVEN a given").createFrom(tableRow, "Given a given").perform();
        findCandidateStep(candidateSteps, "GIVEN a given alias").createFrom(tableRow, "Given a given alias").perform();
        findCandidateStep(candidateSteps, "WHEN a when").createFrom(tableRow, "When a when").perform();
        findCandidateStep(candidateSteps, "WHEN a when alias").createFrom(tableRow, "When a when alias").perform();
        findCandidateStep(candidateSteps, "THEN a then").createFrom(tableRow, "Then a then").perform();
        findCandidateStep(candidateSteps, "THEN a then alias").createFrom(tableRow, "Then a then alias").perform();
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

    @Test
    public void shouldProvideCandidateStepsCorrespondingToAnnotatedStepsInPojo() {
        PojoSteps steps = new PojoSteps();
        CandidateStep[] candidateSteps = new StepsFactory().createCandidateSteps(steps)[0].getSteps();
        assertThat(candidateSteps.length, equalTo(6));

        findCandidateStep(candidateSteps, "GIVEN a given").createFrom(tableRow, "Given a given").perform();
        findCandidateStep(candidateSteps, "GIVEN a given alias").createFrom(tableRow, "Given a given alias").perform();
        findCandidateStep(candidateSteps, "WHEN a when").createFrom(tableRow, "When a when").perform();
        findCandidateStep(candidateSteps, "WHEN a when alias").createFrom(tableRow, "When a when alias").perform();
        findCandidateStep(candidateSteps, "THEN a then").createFrom(tableRow, "Then a then").perform();
        findCandidateStep(candidateSteps, "THEN a then alias").createFrom(tableRow, "Then a then alias").perform();
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

    private CandidateStep findCandidateStep(CandidateStep[] candidateSteps, String candidateStepAsString) {
        for (CandidateStep candidateStep : candidateSteps) {
            if ( candidateStepAsString.equals(candidateStep.toString()) ){
                return candidateStep;
            }
        }
        throw new RuntimeException("CandidateStep "+candidateStepAsString+" not found amongst "+asList(candidateSteps));
    }

    @Test
    public void shouldProvideStepsToBePerformedBeforeStory() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<Step> beforeStory = steps.runBeforeStory(false);
        assertThat(beforeStory.size(), equalTo(1));        
        beforeStory.get(0).perform();
        assertThat(steps.beforeStory, is(true));
        List<Step> beforeEmbeddedStory = steps.runBeforeStory(true);
        assertThat(beforeEmbeddedStory.size(), equalTo(1));        
        beforeEmbeddedStory.get(0).perform();
        assertThat(steps.beforeEmbeddedStory, is(true));
    }
    
    @Test
    public void shouldProvideStepsToBePerformedAfterStory() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<Step> afterStory = steps.runAfterStory(false);
        assertThat(afterStory.size(), equalTo(1));        
        afterStory.get(0).perform();
        assertThat(steps.afterStory, is(true));
        List<Step> afterEmbeddedStory = steps.runAfterStory(true);
        assertThat(afterEmbeddedStory.size(), equalTo(1));        
        afterEmbeddedStory.get(0).perform();
        assertThat(steps.afterEmbeddedStory, is(true));
    }

    
    @Test
    public void shouldProvideStepsToBePerformedBeforeScenarios() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
    	List<Step> executableSteps = steps.runBeforeScenario();
		assertThat(executableSteps.size(), equalTo(1));
		
    	executableSteps.get(0).perform();
    	assertThat(steps.before, is(true));
    }
    
    @Test
    public void shouldProvideStepsToBePerformedAfterScenarios() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
    	List<Step> executableSteps = steps.runAfterScenario();
    	assertThat(executableSteps.size(), equalTo(3));
    	
    	executableSteps.get(0).perform();
    	assertThat(steps.afterAny, is(true));
    	
    	executableSteps.get(1).perform();
    	assertThat(steps.afterSuccess, is(true));
    	
    	executableSteps.get(2).doNotPerform();
    	assertThat(steps.afterFailure, is(true));
    }
    
    @Test
    public void shouldIgnoreSuccessfulStepsWhichArePerformedInUnsuccessfulScenarioOrViceVersa() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
    	List<Step> executableSteps = steps.runAfterScenario();
    	
    	executableSteps.get(0).doNotPerform();
    	assertThat(steps.afterAny, is(true)); // @AfterScenario is run after stories of any outcome
    	
		executableSteps.get(1).doNotPerform();
		assertThat(!steps.afterSuccess, is(true)); // @AfterScenario(uponOutcome=SUCCESS) is run after successful stories
		
		
		executableSteps.get(2).perform();
		assertThat(!steps.afterFailure, is(true)); // @AfterScenario(uponOutcome=FAILURE) is run after unsuccessful stories
	
    }


    @Test(expected=DuplicateCandidateStepFoundException.class)
    public void shouldFailIfDuplicateStepsAreEncountered() {
        DuplicateSteps steps = new DuplicateSteps();
        CandidateStep[] candidateSteps = steps.getSteps();

        assertThat(candidateSteps.length, equalTo(2));
        candidateSteps[0].createFrom(tableRow, "Given a given").perform();

    }

    @Test
    public void shouldAllowI18nOfSteps(){
        StepsConfiguration configuration = new MostUsefulStepsConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
    	I18nSteps steps = new I18nSteps(configuration);
        CandidateStep[] candidateSteps = steps.getSteps();
        assertThat(candidateSteps.length, equalTo(3));

        findCandidateStep(candidateSteps, "GIVEN un dato che").createFrom(tableRow, "Dato che un dato che").perform();
        findCandidateStep(candidateSteps, "WHEN un quando").createFrom(tableRow, "Quando un quando").perform();
        findCandidateStep(candidateSteps, "THEN un allora").createFrom(tableRow, "Allora un allora").perform();

        assertThat(steps.givens, equalTo(1));
        assertThat(steps.whens, equalTo(1));
        assertThat(steps.thens, equalTo(1));    	    	
    }

    @Test(expected=StartingWordNotFound.class)
    public void shouldNotCreateStepIfStartingWordNotFound(){
        StepsConfiguration configuration = new MostUsefulStepsConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
    	I18nSteps steps = new I18nSteps(configuration);
        CandidateStep[] candidateSteps = steps.getSteps();
        assertThat(candidateSteps.length, equalTo(3));

        // misspelled starting word 
        candidateSteps[0].createFrom(tableRow, "Dado che un dato che"); 
        
    }
    
    static class MultipleAliasesSteps extends Steps {
        
        private int givens;
        private int whens;
        private int thens;
        
        private boolean before;
        private boolean afterAny;
        private boolean afterSuccess;
        private boolean afterFailure;
        private boolean beforeStory;
        private boolean afterStory;
        private boolean beforeEmbeddedStory;
        private boolean afterEmbeddedStory;
        
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

        @org.jbehave.core.annotations.BeforeStory(uponEmbedded=true)
        public void beforeEmbeddedStory() {
            beforeEmbeddedStory = true;
        }
        
        @org.jbehave.core.annotations.AfterStory(uponEmbedded=true)
        public void afterEmbeddedStory() {
            afterEmbeddedStory = true;
        }
        
        
        @org.jbehave.core.annotations.BeforeScenario
        public void beforeScenarios() {
        	before = true;
        }
        
        @org.jbehave.core.annotations.AfterScenario
        public void afterAnyScenarios() {
        	afterAny = true;
        }
        
        @org.jbehave.core.annotations.AfterScenario(uponOutcome=AfterScenario.Outcome.SUCCESS)
        public void afterSuccessfulScenarios() {
        	afterSuccess = true;
        }
        
        @org.jbehave.core.annotations.AfterScenario(uponOutcome=AfterScenario.Outcome.FAILURE)
        public void afterUnsuccessfulScenarios() {
        	afterFailure = true;
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

    static class DuplicateSteps extends Steps {
        
        @org.jbehave.core.annotations.Given("a given")
        public void given() {
        }

        @org.jbehave.core.annotations.Given("a given")
        public void duplicateGiven() {
        }
                
    }

    static class I18nSteps extends Steps {

        private int givens;
        private int whens;
        private int thens;

        public I18nSteps(StepsConfiguration configuration) {
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
