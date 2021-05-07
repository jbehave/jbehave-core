package org.jbehave.core.steps;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords.StartingWordNotFound;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Meta;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractCandidateSteps.DuplicateCandidateFound;
import org.jbehave.core.steps.AbstractStepResult.Failed;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.junit.jupiter.api.Test;

class StepsBehaviour {

    private final Map<String, String> tableRow = new HashMap<>();

    @Test
    void shouldListCandidateStepsFromAnnotatedMethodsWithSingleAlias() {
        SingleAliasSteps steps = new SingleAliasSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(6));

        performMatchedStep(candidates, "GIVEN a given", "Given a given");
        performMatchedStep(candidates, "GIVEN a given alias", "Given a given alias");
        performMatchedStep(candidates, "WHEN a when", "When a when");
        performMatchedStep(candidates, "WHEN a when alias", "When a when alias");
        performMatchedStep(candidates, "THEN a then", "Then a then");
        performMatchedStep(candidates, "THEN a then alias", "Then a then alias");
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

    @Test
    void shouldListCandidateStepsFromAnnotatedMethodsWithMultipleAliases() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(9));
        
        performMatchedStep(candidates, "GIVEN a given", "Given a given");
        performMatchedStep(candidates, "GIVEN a given alias", "Given a given alias");
        performMatchedStep(candidates, "GIVEN another given alias", "Given another given alias");
        performMatchedStep(candidates, "WHEN a when", "When a when");
        performMatchedStep(candidates, "WHEN a when alias", "When a when alias");
        performMatchedStep(candidates, "WHEN another when alias", "When another when alias");
        performMatchedStep(candidates, "THEN a then", "Then a then");
        performMatchedStep(candidates, "THEN a then alias", "Then a then alias");
        performMatchedStep(candidates, "THEN another then alias", "Then another then alias");

        assertThat(steps.givens, equalTo(3));
        assertThat(steps.whens, equalTo(3));
        assertThat(steps.thens, equalTo(3));
    }

    @Test
    void shouldListCandidateStepsFromAnnotatedMethodsInPojo() {
        PojoSteps steps = new PojoSteps();
        Configuration configuration = new MostUsefulConfiguration();
        List<StepCandidate> candidates = new InstanceStepsFactory(configuration, steps).createCandidateSteps().get(0).listCandidates();
        assertThat(candidates.size(), equalTo(6));

        performMatchedStep(candidates, "GIVEN a given", "Given a given");
        performMatchedStep(candidates, "GIVEN a given alias", "Given a given alias");
        performMatchedStep(candidates, "WHEN a when", "When a when");
        performMatchedStep(candidates, "WHEN a when alias", "When a when alias");
        performMatchedStep(candidates, "THEN a then", "Then a then");
        performMatchedStep(candidates, "THEN a then alias", "Then a then alias");
        
        assertThat(steps.givens, equalTo(2));
        assertThat(steps.whens, equalTo(2));
        assertThat(steps.thens, equalTo(2));
    }

    private void performMatchedStep(List<StepCandidate> candidates, String candidateAsString, String stepAsString) {
        StoryReporter reporter = mock(StoryReporter.class);
        findCandidate(candidates, candidateAsString).createMatchedStep(stepAsString, tableRow, Collections.emptyList())
                .perform(reporter, null);
        verify(reporter).beforeStep(argThat(arg -> stepAsString.equals(arg.getStepAsString())
                && StepExecutionType.EXECUTABLE.equals(arg.getExecutionType())));
    }

    private StepCandidate findCandidate(List<StepCandidate> candidates, String candidateAsString) {
        for (StepCandidate candidate : candidates) {
            if (candidateAsString.equals(candidate.toString())) {
                return candidate;
            }
        }
        throw new RuntimeException("StepCandidate "+candidateAsString + " not found amongst " +candidates);
    }

    @Test
    void shouldListStepsToBePerformedBeforeStories() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<BeforeOrAfterStep> beforeStories = steps.listBeforeStories();
        assertThat(beforeStories.size(), equalTo(1));
        beforeStories.get(0).createStep().perform(null, null);
        assertThat(beforeStories.get(0).getMethod().getName(), equalTo("beforeStories"));
        assertThat(steps.beforeStories, is(true));
    }

    @Test
    void shouldListStepsToBePerformedAfterStories() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<BeforeOrAfterStep> afterStories = steps.listAfterStories();
        assertThat(afterStories.size(), equalTo(1));
        afterStories.get(0).createStep().perform(null, null);
        assertThat(afterStories.get(0).getMethod().getName(), equalTo("afterStories"));
        assertThat(steps.afterStories, is(true));
    }

    @Test
    void shouldListStepsToBePerformedBeforeAndAfterStory() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();

        List<BeforeOrAfterStep> beforeStory = steps.listBeforeStory(false);
        assertThat(beforeStory.size(), equalTo(1));
        beforeStory.get(0).createStep().perform(null, null);
        assertThat(beforeStory.get(0).getMethod().getName(), equalTo("beforeStory"));
        assertThat(steps.beforeStory, is(true));

        List<BeforeOrAfterStep> afterStory = steps.listAfterStory(false);
        assertThat(afterStory.size(), equalTo(1));
        afterStory.get(0).createStep().perform(null, null);
        assertThat(afterStory.get(0).getMethod().getName(), equalTo("afterStory"));
        assertThat(steps.afterStory, is(true));
        
        List<BeforeOrAfterStep> beforeGivenStory = steps.listBeforeStory(true);
        assertThat(beforeGivenStory.size(), equalTo(1));
        beforeGivenStory.get(0).createStep().perform(null, null);
        assertThat(steps.beforeGivenStory, is(true));

        List<BeforeOrAfterStep> afterGivenStory = steps.listAfterStory(true);
        assertThat(afterGivenStory.size(), equalTo(1));
        afterGivenStory.get(0).createStep().perform(null, null);
        assertThat(steps.afterGivenStory, is(true));
    }
    
    @Test
    void shouldProvideStepsToBePerformedBeforeAndAfterScenariosWithFailureOccuring() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeScenario = steps.listBeforeScenario().get(scenarioType);
        assertThat(beforeScenario.size(), equalTo(1));

        beforeScenario.get(0).createStep().perform(null, null);
        assertThat(steps.beforeNormalScenario, is(true));

        List<BeforeOrAfterStep> afterScenario = steps.listAfterScenario().get(scenarioType);
        assertThat(afterScenario.size(), equalTo(3));
        Meta storyAndScenarioMeta = null;
        // uponOutcome=ANY
        afterScenario.get(0).createStepUponOutcome(storyAndScenarioMeta).perform(null, null);
        assertThat(steps.afterNormalScenario, is(true));

        // uponOutcome=SUCCESS
        afterScenario.get(1).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null, null);
        assertThat(steps.afterSuccessfulScenario, is(false));

        // uponOutcome=FAILURE
        afterScenario.get(2).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null, null);
        assertThat(steps.afterFailedScenario, is(true));
    }

    @Test
    void shouldProvideStepsToBePerformedBeforeAndAfterScenariosWithNoFailureOccuring() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeScenario = steps.listBeforeScenario().get(scenarioType);
        assertThat(beforeScenario.size(), equalTo(1));

        beforeScenario.get(0).createStep().perform(null, null);
        assertThat(steps.beforeNormalScenario, is(true));

        List<BeforeOrAfterStep> afterScenario = steps.listAfterScenario().get(scenarioType);
        assertThat(afterScenario.size(), equalTo(3));
        Meta storyAndScenarioMeta = null;
        // uponOutcome=ANY
        afterScenario.get(0).createStepUponOutcome(storyAndScenarioMeta).perform(null, null);
        assertThat(steps.afterNormalScenario, is(true));

        // uponOutcome=SUCCESS
        afterScenario.get(1).createStepUponOutcome(storyAndScenarioMeta).perform(null, null);
        assertThat(steps.afterSuccessfulScenario, is(true));

        // uponOutcome=FAILURE
        afterScenario.get(2).createStepUponOutcome(storyAndScenarioMeta).perform(null, null);
        assertThat(steps.afterFailedScenario, is(false));

    }
        
    @Test
    void shouldProvideStepsToBeNotPerformedAfterScenarioUponOutcome() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeScenario = steps.listBeforeScenario().get(scenarioType);
        assertThat(beforeScenario.size(), equalTo(1));

        beforeScenario.get(0).createStep().doNotPerform(null, null);
        assertThat(steps.beforeNormalScenario, is(true));

        List<BeforeOrAfterStep> afterScenario = steps.listAfterScenario().get(scenarioType);
        assertThat(afterScenario.size(), equalTo(3));
        Meta storyAndScenarioMeta = null;
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper();
        // uponOutcome=ANY
        afterScenario.get(0).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null, failure);
        assertThat(steps.afterNormalScenario, is(true));
        
        // uponOutcome=SUCCESS
        afterScenario.get(1).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null, failure);
        assertThat(steps.afterSuccessfulScenario, is(false));
        
        // uponOutcome=FAILURE        
        afterScenario.get(2).createStepUponOutcome(storyAndScenarioMeta).doNotPerform(null, failure);
        assertThat(steps.afterFailedScenario, is(true));
    }

    @Test
    void shouldProvideStepsToBePerformedBeforeAndAfterScenariosParametrisedByExample() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.EXAMPLE;
        List<BeforeOrAfterStep> beforeScenario = steps.listBeforeScenario().get(scenarioType);
        assertThat(beforeScenario.size(), equalTo(1));

        beforeScenario.get(0).createStep().perform(null, null);
        assertThat(steps.beforeExampleScenario, is(true));

        List<BeforeOrAfterStep> afterScenario = steps.listAfterScenario().get(scenarioType);
        assertThat(afterScenario.size(), equalTo(1));

        afterScenario.get(0).createStep().perform(null, null);
        assertThat(steps.afterExampleScenario, is(true));
    }

    @Test
    void shouldProvideStepsToBePerformedBeforeAndAfterAnyScenario() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        ScenarioType scenarioType = ScenarioType.ANY;
        List<BeforeOrAfterStep> beforeScenario = steps.listBeforeScenario().get(scenarioType);
        assertThat(beforeScenario.size(), equalTo(1));

        beforeScenario.get(0).createStep().perform(null, null);
        assertThat(steps.beforeAnyScenario, is(true));

        List<BeforeOrAfterStep> afterScenario = steps.listAfterScenario().get(scenarioType);
        assertThat(afterScenario.size(), equalTo(1));

        afterScenario.get(0).createStep().perform(null, null);
        assertThat(steps.afterAnyScenario, is(true));
    }

    @Test
    void shouldAllowBeforeOrAfterStepsToUseSpecifiedStepMonitor() {
        MultipleAliasesSteps steps = new MultipleAliasesSteps();
        List<BeforeOrAfterStep> beforeStory = steps.listBeforeStory(false);
        BeforeOrAfterStep step = beforeStory.get(0);
        StepMonitor stepMonitor = new PrintStreamStepMonitor();
        step.useStepMonitor(stepMonitor);
        assertThat(step.toString(), Matchers.containsString(stepMonitor.getClass().getName()));
    }

    @Test
    void shouldAllowLocalizationOfSteps() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
        LocalizedSteps steps = new LocalizedSteps(configuration);
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(3));

        performMatchedStep(candidates, "GIVEN un dato che", "Dato che un dato che");
        performMatchedStep(candidates, "WHEN un quando", "Quando un quando");
        performMatchedStep(candidates, "THEN un allora", "Allora un allora");

        assertThat(steps.givens, equalTo(1));
        assertThat(steps.whens, equalTo(1));
        assertThat(steps.thens, equalTo(1));
    }

    @Test
    void shouldReportFailuresInBeforeMethods() {
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeOrAfterStepList = new BeforeSteps().listBeforeScenario().get(scenarioType);
        StepResult stepResult = beforeOrAfterStepList.get(0).createStep().perform(null, null);
        assertThat(stepResult, instanceOf(Failed.class));
        assertThat(stepResult.getFailure(), instanceOf(UUIDExceptionWrapper.class));
        assertThat(stepResult.getFailure().getCause(), instanceOf(BeforeOrAfterFailed.class));
    }

    @Test
    void shouldReportFailuresInAfterMethods() {
        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<BeforeOrAfterStep> beforeOrAfterStepList = new AfterSteps().listAfterScenario().get(scenarioType);
        StepResult stepResult = beforeOrAfterStepList.get(0).createStep().perform(null, null);
        assertThat(stepResult, instanceOf(Failed.class));
        assertThat(stepResult.getFailure(), instanceOf(UUIDExceptionWrapper.class));
        assertThat(stepResult.getFailure().getCause(), instanceOf(BeforeOrAfterFailed.class));
    }

    @Test
    void shouldFailIfDuplicateStepsAreEncountered() {
        DuplicateSteps steps = new DuplicateSteps();
        assertThrows(DuplicateCandidateFound.class, steps::listCandidates);
    }

    @Test
    void shouldFailIfDuplicateStepsWithDifferentParamsNamesAreEncountered() {
        DuplicateStepsWithParameters steps = new DuplicateStepsWithParameters();
        assertThrows(DuplicateCandidateFound.class, steps::listCandidates);
    }

    @Test
    void shouldNotFailWithDuplicateCandidateFoundExceptionIfStepsWordingsDoNotMatchEachOther() {
        StepsWithParameters steps = new StepsWithParameters();
        Configuration configuration = new MostUsefulConfiguration();
        List<StepCandidate> candidates = new InstanceStepsFactory(configuration, steps).createCandidateSteps().get(0).listCandidates();
        assertThat(candidates.size(), equalTo(2));

        performMatchedStep(candidates, "GIVEN a given param '$someParameterName'",
                "Given a given param '$someParameterName'");
        performMatchedStep(candidates, "GIVEN a given param '$givenParameter' and '$secondParam'",
                "Given a given param '$givenParameter' and '$secondParam'");
    }

    @Test
    void shouldNotCreateStepIfStartingWordNotFound() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
        LocalizedSteps steps = new LocalizedSteps(configuration);
        List<StepCandidate> candidates = steps.listCandidates();
        assertThat(candidates.size(), equalTo(3));

        // misspelled starting word 
        StepCandidate stepCandidate = candidates.get(0);
        List<Step> composedSteps = Collections.emptyList();
        assertThrows(StartingWordNotFound.class,
                () -> stepCandidate.createMatchedStep("Dado che un dato che", tableRow, composedSteps));
    }

    @Test
    void shouldListBeforeStoriesAccordingToTheirOrder() {
        OrderedSteps steps = new OrderedSteps();
        List<BeforeOrAfterStep> beforeStories = steps.listBeforeStories();
        assertThat(beforeStories.size(), equalTo(3));
        assertStepName(beforeStories.get(0), "beforeStoriesOrderTwo");
        assertStepName(beforeStories.get(1), "beforeStoriesOrderOne");
        assertStepName(beforeStories.get(2), "beforeStoriesOrderDefault");
    }

    @Test
    void shouldListAfterStoriesAccordingToTheirOrder() {
        OrderedSteps steps = new OrderedSteps();
        List<BeforeOrAfterStep> afterStories = steps.listAfterStories();
        assertThat(afterStories.size(), equalTo(3));
        assertStepName(afterStories.get(0), "afterStoriesOrderDefault");
        assertStepName(afterStories.get(1), "afterStoriesOrderOne");
        assertStepName(afterStories.get(2), "afterStoriesOrderTwo");
    }

    private void assertStepName(BeforeOrAfterStep step, String name) {
        String method = step.getMethod().getName();
        assertThat(method, equalTo(name));
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
        
        @Given("a given")
        @Aliases(values={"a given alias", "another given alias"})
        public void given() {
            givens++;
        }

        @When("a when")
        @Aliases(values={"a when alias", "another when alias"})
        public void when() {
            whens++;
        }
        
        @Then("a then")
        @Aliases(values={"a then alias", "another then alias"})
        public void then() {
            thens++;
        }

        @BeforeStories
        public void beforeStories() {
            beforeStories = true;
        }
        
        @AfterStories
        public void afterStories() {
            afterStories = true;
        }

        @BeforeStory
        public void beforeStory() {
            beforeStory = true;
        }
        
        @AfterStory
        public void afterStory() {
            afterStory = true;
        }

        @BeforeStory(uponGivenStory=true)
        public void beforeGivenStory() {
            beforeGivenStory = true;
        }
        
        @AfterStory(uponGivenStory=true)
        public void afterGivenStory() {
            afterGivenStory = true;
        }        
        
        @BeforeScenario(uponType = ScenarioType.NORMAL)
        public void beforeNormalScenarios() {
            beforeNormalScenario = true;
        }

        @BeforeScenario(uponType=ScenarioType.EXAMPLE)
        public void beforeExampleScenarios() {
            beforeExampleScenario = true;
        }

        @BeforeScenario(uponType=ScenarioType.ANY)
        public void beforeAnyScenarios() {
            beforeAnyScenario = true;
        }

        @AfterScenario(uponType = ScenarioType.NORMAL)
        public void afterNormalScenarios() {
            afterNormalScenario = true;
        }

        @AfterScenario(uponType=ScenarioType.EXAMPLE)
        public void afterExampleScenarios() {
            afterExampleScenario = true;
        }

        @AfterScenario(uponType=ScenarioType.ANY)
        public void afterAnyScenarios() {
            afterAnyScenario = true;
        }

        @AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome=AfterScenario.Outcome.SUCCESS)
        public void afterSuccessfulScenarios() {
            afterSuccessfulScenario = true;
        }
        
        @AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome=AfterScenario.Outcome.FAILURE)
        public void afterFailedScenarios() {
            afterFailedScenario = true;
        }
        
        
    }

    static class SingleAliasSteps extends Steps {

        private int givens;
        private int whens;
        private int thens;

        @Given("a given")
        @Alias("a given alias")
        public void given() {
            givens++;
        }

        @When("a when")
        @Alias("a when alias")
        public void when() {
            whens++;
        }

        @Then("a then")
        @Alias("a then alias")
        public void then() {
            thens++;
        }

    }

    static class PojoSteps {

        private int givens;
        private int whens;
        private int thens;

        @Given("a given")
        @Alias("a given alias")
        public void given() {
            givens++;
        }

        @When("a when")
        @Alias("a when alias")
        public void when() {
            whens++;
        }

        @Then("a then")
        @Alias("a then alias")
        public void then() {
            thens++;
        }

    }

    static class BeforeSteps extends Steps {
        
        @BeforeScenario(uponType = ScenarioType.NORMAL)
        public void beforeScenario() {
            throw new RuntimeException("Damn, I failed!");
        }

    }

    static class AfterSteps extends Steps {

        @AfterScenario(uponType = ScenarioType.NORMAL)
        public void afterScenario() {
            throw new RuntimeException("Damn, I failed!");
        }

    }

    static class DuplicateSteps extends Steps {
        
        @Given("a given")
        public void given() {
        }

        @Given("a given")
        public void duplicateGiven() {
        }
                
    }

    static class DuplicateStepsWithParameters extends Steps {
        
        @Given("a given param '$someParameterName'")
        public void given(String someParameterName) {
        }

        @Given("a given param '$givenParameter'")
        public void duplicateGiven(String givenParameter) {
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

    static class LocalizedSteps extends Steps {

        private int givens;
        private int whens;
        private int thens;

        public LocalizedSteps(Configuration configuration) {
            super(configuration);
        }

        @Given("un dato che")
        public void given() {
            givens++;
        }

        @When("un quando")
        public void when() {
            whens++;
        }

        @Then("un allora")
        public void then() {
            thens++;
        }

    }

    static class OrderedSteps extends Steps {

        @BeforeStories
        public void beforeStoriesOrderDefault() {
        }

        @AfterStories
        public void afterStoriesOrderDefault() {
        }

        @BeforeStories(order = 1)
        public void beforeStoriesOrderOne() {
        }

        @AfterStories(order = 1)
        public void afterStoriesOrderOne() {
        }

        @BeforeStories(order = 2)
        public void beforeStoriesOrderTwo() {
        }

        @AfterStories(order = 2)
        public void afterStoriesOrderTwo() {
        }

    }
}
