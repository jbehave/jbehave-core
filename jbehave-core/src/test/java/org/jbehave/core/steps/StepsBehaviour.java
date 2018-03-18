package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords.StartingWordNotFound;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Meta;
import org.jbehave.core.steps.AbstractStepResult.Failed;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Steps.DuplicateCandidateFound;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class StepsBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldListCandidateStepsFromAnnotatedMethodsWithSingleAlias() {
        SingleAliasSteps steps = new SingleAliasSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(6));

        findCandidate(candidates, "GIVEN a given").createMatchedStep("Given a given", tableRow).perform(null);
        findCandidate(candidates, "GIVEN a given alias").createMatchedStep("Given a given alias", tableRow).perform(null);
        findCandidate(candidates, "WHEN a when").createMatchedStep("When a when", tableRow).perform(null);
        findCandidate(candidates, "WHEN a when alias").createMatchedStep("When a when alias", tableRow).perform(null);
        findCandidate(candidates, "THEN a then").createMatchedStep("Then a then", tableRow).perform(null);
        findCandidate(candidates, "THEN a then alias").createMatchedStep("Then a then alias", tableRow).perform(null);
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

	@Test
    public void shouldListCandidateStepsFromAnnotatedMethodsWithMultipleAliases() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(9));
        
        findCandidate(candidates, "GIVEN a given").createMatchedStep("Given a given", tableRow).perform(null);
        findCandidate(candidates, "GIVEN a given alias").createMatchedStep("Given a given alias", tableRow).perform(null);
        findCandidate(candidates, "GIVEN another given alias").createMatchedStep("Given another given alias", tableRow).perform(null);
        findCandidate(candidates, "WHEN a when").createMatchedStep("When a when", tableRow).perform(null);
        findCandidate(candidates, "WHEN a when alias").createMatchedStep("When a when alias", tableRow).perform(null);
        findCandidate(candidates, "WHEN another when alias").createMatchedStep("When another when alias", tableRow).perform(null);
        findCandidate(candidates, "THEN a then").createMatchedStep("Then a then", tableRow).perform(null);
        findCandidate(candidates, "THEN a then alias").createMatchedStep("Then a then alias", tableRow).perform(null);
        findCandidate(candidates, "THEN another then alias").createMatchedStep("Then another then alias", tableRow).perform(null);

        assertThat(steps.givens, equalTo(3));
        assertThat(steps.whens, equalTo(3));
        assertThat(steps.thens, equalTo(3));
    }

    @Test
    public void shouldListCandidateStepsFromAnnotatedMethodsInPojo() {
        PojoSteps steps = new PojoSteps();
        Configuration configuration = new MostUsefulConfiguration();
		List<StepCandidate> candidates = new InstanceStepsFactory(configuration, steps).createCandidateSteps().get(0).listCandidates();
        assertThat(candidates.size(), equalTo(6));

        findCandidate(candidates, "GIVEN a given").createMatchedStep("Given a given", tableRow).perform(null);
        findCandidate(candidates, "GIVEN a given alias").createMatchedStep("Given a given alias", tableRow).perform(null);
        findCandidate(candidates, "WHEN a when").createMatchedStep("When a when", tableRow).perform(null);
        findCandidate(candidates, "WHEN a when alias").createMatchedStep("When a when alias", tableRow).perform(null);
        findCandidate(candidates, "THEN a then").createMatchedStep("Then a then", tableRow).perform(null);
        findCandidate(candidates, "THEN a then alias").createMatchedStep("Then a then alias", tableRow).perform(null);
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

    private StepCandidate findCandidate(List<StepCandidate> candidates, String candidateAsString) {
        for (StepCandidate candidate : candidates) {
            if ( candidateAsString.equals(candidate.toString()) ){
                return candidate;
            }
        }
        throw new RuntimeException("StepCandidate "+candidateAsString+" not found amongst "+candidates);
    }

    @Test
    public void shouldListStepsToBePerformedBeforeAndAfterStories() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<BeforeOrAfterStep> beforeAfterStories = steps.listBeforeOrAfterStories();
        assertThat(beforeAfterStories.size(), equalTo(2));        
        beforeAfterStories.get(0).createStep().perform(null);
        assertThat(beforeAfterStories.get(0).getStage(), equalTo(Stage.BEFORE));
        assertThat(beforeAfterStories.get(0).getMethod().getName(), equalTo("beforeStories"));
        assertThat(steps.beforeStories, is(true));
        beforeAfterStories.get(1).createStep().perform(null);
        assertThat(beforeAfterStories.get(1).getStage(), equalTo(Stage.AFTER));
        assertThat(beforeAfterStories.get(1).getMethod().getName(), equalTo("afterStories"));
        assertThat(steps.afterStories, is(true));
        
    }

    @Test
    public void shouldListStepsToBePerformedBeforeAndAfterStory() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<BeforeOrAfterStep> beforeAfterStory = steps.listBeforeOrAfterStory(false);
        assertThat(beforeAfterStory.size(), equalTo(2));        
        beforeAfterStory.get(0).createStep().perform(null);
        assertThat(beforeAfterStory.get(0).getStage(), equalTo(Stage.BEFORE));
        assertThat(beforeAfterStory.get(0).getMethod().getName(), equalTo("beforeStory"));
        assertThat(steps.beforeStory, is(true));
        beforeAfterStory.get(1).createStep().perform(null);
        assertThat(beforeAfterStory.get(1).getStage(), equalTo(Stage.AFTER));
        assertThat(beforeAfterStory.get(1).getMethod().getName(), equalTo("afterStory"));
        assertThat(steps.afterStory, is(true));
        
        List<BeforeOrAfterStep> beforeAfterGivenStory = steps.listBeforeOrAfterStory(true);
        assertThat(beforeAfterGivenStory.size(), equalTo(2));        
        beforeAfterGivenStory.get(0).createStep().perform(null);
        assertThat(steps.beforeGivenStory, is(true));
        beforeAfterGivenStory.get(1).createStep().perform(null);
        assertThat(steps.afterGivenStory, is(true));
    }
    
    @Test
    public void shouldProvideStepsToBePerformedBeforeAndAfterScenariosWithFailureOccuring() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeAfterScenario = steps.listBeforeOrAfterScenario(scenarioType);
        assertThat(beforeAfterScenario.size(), equalTo(4));

        beforeAfterScenario.get(0).createStep().perform(null);
        assertThat(steps.beforeNormalScenario, is(true));

        Meta storyAndScenarioMeta = null;
        // uponOutcome=ANY
        beforeAfterScenario.get(1).createStepUponOutcome(storyAndScenarioMeta).perform(null);
        assertThat(steps.afterNormalScenario, is(true));

        // uponOutcome=SUCCESS
        beforeAfterScenario.get(2).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null);
        assertThat(steps.afterSuccessfulScenario, is(false));

        // uponOutcome=FAILURE
        beforeAfterScenario.get(3).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null);
        assertThat(steps.afterFailedScenario, is(true));
    }

    @Test
    public void shouldProvideStepsToBePerformedBeforeAndAfterScenariosWithNoFailureOccuring() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeAfterScenario = steps.listBeforeOrAfterScenario(scenarioType);
        assertThat(beforeAfterScenario.size(), equalTo(4));
        
        beforeAfterScenario.get(0).createStep().perform(null);
        assertThat(steps.beforeNormalScenario, is(true));

        Meta storyAndScenarioMeta = null;
        // uponOutcome=ANY
        beforeAfterScenario.get(1).createStepUponOutcome(storyAndScenarioMeta).perform(null);
        assertThat(steps.afterNormalScenario, is(true));
        
        // uponOutcome=SUCCESS
        beforeAfterScenario.get(2).createStepUponOutcome(storyAndScenarioMeta).perform(null);
        assertThat(steps.afterSuccessfulScenario, is(true));
        
        // uponOutcome=FAILURE      
        beforeAfterScenario.get(3).createStepUponOutcome(storyAndScenarioMeta).perform(null);
        assertThat(steps.afterFailedScenario, is(false));

    }
        
    @Test
    public void shouldProvideStepsToBeNotPerformedAfterScenarioUponOutcome() {
    	MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeAfterScenario = steps.listBeforeOrAfterScenario(scenarioType);
		assertThat(beforeAfterScenario.size(), equalTo(4));
		
    	beforeAfterScenario.get(0).createStep().doNotPerform(null);
    	assertThat(steps.beforeNormalScenario, is(true));

        Meta storyAndScenarioMeta = null;
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper();
        // uponOutcome=ANY
        beforeAfterScenario.get(1).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(failure);
    	assertThat(steps.afterNormalScenario, is(true));
    	
    	// uponOutcome=SUCCESS
        beforeAfterScenario.get(2).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(failure);
    	assertThat(steps.afterSuccessfulScenario, is(false));
    	
		// uponOutcome=FAILURE    	
        beforeAfterScenario.get(3).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(failure);
    	assertThat(steps.afterFailedScenario, is(true));
    }

    @Test
    public void shouldProvideStepsToBePerformedBeforeAndAfterScenariosParametrisedByExample() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.EXAMPLE;
        List<BeforeOrAfterStep> beforeAfterScenario = steps.listBeforeOrAfterScenario(scenarioType);
        assertThat(beforeAfterScenario.size(), equalTo(2));
        
        beforeAfterScenario.get(0).createStep().perform(null);
        assertThat(steps.beforeExampleScenario, is(true));

        beforeAfterScenario.get(1).createStep().perform(null);
        assertThat(steps.afterExampleScenario, is(true));

    }

    @Test
    public void shouldProvideStepsToBePerformedBeforeAndAfterAnyScenario() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.ANY;
        List<BeforeOrAfterStep> beforeAfterScenario = steps.listBeforeOrAfterScenario(scenarioType);
        assertThat(beforeAfterScenario.size(), equalTo(2));

        beforeAfterScenario.get(0).createStep().perform(null);
        assertThat(steps.beforeAnyScenario, is(true));

        beforeAfterScenario.get(1).createStep().perform(null);
        assertThat(steps.afterAnyScenario, is(true));

    }

    @Test
    public void shouldAllowBeforeOrAfterStepsToUseSpecifiedStepMonitor() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<BeforeOrAfterStep> beforeAfterStory = steps.listBeforeOrAfterStory(false);
        BeforeOrAfterStep step = beforeAfterStory.get(0);
        StepMonitor stepMonitor = new PrintStreamStepMonitor();
        step.useStepMonitor(stepMonitor);
        assertThat(step.toString(), Matchers.containsString(stepMonitor.getClass().getName()));
    }

    @Test
    public void shouldAllowLocalizationOfSteps(){
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
    	LocalizedSteps steps = new LocalizedSteps(configuration);
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(3));

        findCandidate(candidates, "GIVEN un dato che").createMatchedStep("Dato che un dato che", tableRow).perform(null);
        findCandidate(candidates, "WHEN un quando").createMatchedStep("Quando un quando", tableRow).perform(null);
        findCandidate(candidates, "THEN un allora").createMatchedStep("Allora un allora", tableRow).perform(null);

        assertThat(steps.givens, equalTo(1));
        assertThat(steps.whens, equalTo(1));
        assertThat(steps.thens, equalTo(1));    	    	
    }

    @Test
    public void shouldReportFailuresInBeforeMethods() {
        assertFailureReturnedOnStepsPerformed(new BeforeSteps());
    }

    @Test
    public void shouldReportFailuresInAfterMethods() {
        assertFailureReturnedOnStepsPerformed(new AfterSteps());
    }

    private void assertFailureReturnedOnStepsPerformed(Steps steps) {
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeOrAfterStepList = steps.listBeforeOrAfterScenario(scenarioType);
        StepResult stepResult = beforeOrAfterStepList.get(0).createStep().perform(null);
        assertThat(stepResult, instanceOf(Failed.class));
        assertThat(stepResult.getFailure(), instanceOf(UUIDExceptionWrapper.class));
        assertThat(stepResult.getFailure().getCause(), instanceOf(BeforeOrAfterFailed.class));
    }

    @Test(expected=DuplicateCandidateFound.class)
    public void shouldFailIfDuplicateStepsAreEncountered() {
        DuplicateSteps steps = new DuplicateSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(2));
        candidates.get(0).createMatchedStep("Given a given", tableRow).perform(null);

    }

    @Test(expected=StartingWordNotFound.class)
    public void shouldNotCreateStepIfStartingWordNotFound(){
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
    	LocalizedSteps steps = new LocalizedSteps(configuration);
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(3));

        // misspelled starting word 
        candidates.get(0).createMatchedStep("Dado che un dato che", tableRow); 
        
    }
    
    static class MultipleAliasesSteps extends Steps {
        
        private int givens;
        private int whens;
        private int thens;
        
        private boolean beforeNormalScenario;
        private boolean afterNormalScenario;
        private boolean afterSuccessfulScenario;
        private boolean afterFailedScenario;
        private boolean beforeExampleScenario;
        private boolean afterExampleScenario;
        private boolean beforeAnyScenario;
        private boolean afterAnyScenario;
        private boolean beforeStory;
        private boolean afterStory;
        private boolean beforeGivenStory;
        private boolean afterGivenStory;
        private boolean beforeStories;
        private boolean afterStories;
        
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

        @org.jbehave.core.annotations.BeforeStories
        public void beforeStories() {
            beforeStories = true;
        }
        
        @org.jbehave.core.annotations.AfterStories
        public void afterStories() {
            afterStories = true;
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
        public void beforeNormalScenarios() {
        	beforeNormalScenario = true;
        }

        @org.jbehave.core.annotations.BeforeScenario(uponType=ScenarioType.EXAMPLE)
        public void beforeExampleScenarios() {
            beforeExampleScenario = true;
        }

        @org.jbehave.core.annotations.BeforeScenario(uponType=ScenarioType.ANY)
        public void beforeAnyScenarios() {
            beforeAnyScenario = true;
        }

        @org.jbehave.core.annotations.AfterScenario
        public void afterNormalScenarios() {
        	afterNormalScenario = true;
        }

        @org.jbehave.core.annotations.AfterScenario(uponType=ScenarioType.EXAMPLE)
        public void afterExampleScenarios() {
            afterExampleScenario = true;
        }

        @org.jbehave.core.annotations.AfterScenario(uponType=ScenarioType.ANY)
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

    static class BeforeSteps extends Steps {
        
        @org.jbehave.core.annotations.BeforeScenario
        public void beforeScenario() {
            throw new RuntimeException("Damn, I failed!");
        }

    }

    static class AfterSteps extends Steps {

        @org.jbehave.core.annotations.AfterScenario
        public void afterScenario() {
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
