package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.jbehave.core.steps.AbstractStepResult.failed;
import static org.jbehave.core.steps.AbstractStepResult.notPerformed;
import static org.jbehave.core.steps.AbstractStepResult.pending;
import static org.jbehave.core.steps.AbstractStepResult.successful;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepResult;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.Test;
import org.mockito.InOrder;

public class StoryRunnerBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldRunStepsBeforeAndAfterStories() throws Throwable {
        // Given
        Step beforeStep = mock(Step.class);
        StepResult beforeResult = mock(StepResult.class);
        when(beforeStep.perform()).thenReturn(beforeResult);
        Step afterStep = mock(Step.class);
        StepResult afterResult = mock(StepResult.class);
        when(afterStep.perform()).thenReturn(afterResult);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        StoryReporter reporter = mock(StoryReporter.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.BEFORE)).thenReturn(asList(beforeStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.BEFORE);
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.AFTER)).thenReturn(asList(afterStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.AFTER);

        // Then
        verify(beforeStep).perform();
        verify(afterStep).perform();
    }
    
    @Test
    public void shouldRunStepsInStoryAndReportResultsToReporter() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("my title 1", asList("failingStep",
                "successfulStep"));
        Scenario scenario2 = new Scenario("my title 2", asList("successfulStep"));
        Scenario scenario3 = new Scenario("my title 3", asList("successfulStep",
                "pendingStep"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1,
                scenario2, scenario3));
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        IllegalArgumentException anException = new IllegalArgumentException();
        Step pendingStep = mock(Step.class);
        Step successfulStep = mock(Step.class);
        Step failingStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(pending("pendingStep"));
        when(successfulStep.perform()).thenReturn(successful("successfulStep"));
        when(successfulStep.doNotPerform()).thenReturn(notPerformed("successfulStep"));
        when(failingStep.perform()).thenReturn(failed("failingStep", anException));
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, tableRow)).thenReturn(
                asList(failingStep, successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, tableRow)).thenReturn(asList(successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario3, tableRow)).thenReturn(
                asList(successfulStep, pendingStep));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, failureStrategy), asList(mySteps), story, new MetaFilter());

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(story, false);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).failed("failingStep", anException);
        inOrder.verify(reporter).notPerformed("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 2");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 3");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).pending("pendingStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(false);
        inOrder.verify(failureStrategy).handleFailure(anException);
    }

    @Test
    public void shouldRunGivenStoriesBeforeSteps() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("core 1", asList("successfulStep"));
        List<String> givenStories = asList("/path/to/given/story1");
        Scenario scenario2 = new Scenario("core 2", givenStories,
                asList("anotherSuccessfulStep"));
        Story story1 = new Story(new Description("story 1"), Narrative.EMPTY, asList(scenario1));
        Story story2 = new Story(new Description("story 2"), Narrative.EMPTY, asList(scenario2));

        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);

        StoryParser storyParser = mock(StoryParser.class);
        StoryLoader storyLoader = mock(StoryLoader.class);
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Step successfulStep = mock(Step.class);
        when(successfulStep.perform()).thenReturn(successful("successfulStep"));
        Step anotherSuccessfulStep = mock(Step.class);
        when(anotherSuccessfulStep.perform()).thenReturn(successful("anotherSuccessfulStep"));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story1, givenStory, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, tableRow)).thenReturn(asList(successfulStep));
        givenStoryWithNoBeforeOrAfterSteps(story2, givenStory, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, tableRow)).thenReturn(
                asList(anotherSuccessfulStep));
        when(storyLoader.loadStoryAsText("/path/to/given/story1")).thenReturn("storyContent");
        when(storyParser.parseStory("storyContent", "/path/to/given/story1")).thenReturn(story1);
        givenStoryWithNoBeforeOrAfterSteps(story1, givenStory, collector, mySteps);
        givenStoryWithNoBeforeOrAfterSteps(story2, givenStory, collector, mySteps);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(storyParser, storyLoader, reporter, collector, failureStrategy), asList(mySteps),
                 story2, new MetaFilter());

        // Then
        InOrder inOrder = inOrder(reporter);
        inOrder.verify(reporter).beforeStory(story2, givenStory);
        inOrder.verify(reporter).givenStories(givenStories);
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).successful("anotherSuccessfulStep");
        inOrder.verify(reporter).afterStory(givenStory);
        verify(reporter, never()).beforeStory(story1, givenStory);
    }

    @Test
    public void shouldNotPerformStepsAfterStepsWhichShouldNotContinue() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepNormal = mock(Step.class);
        Step secondStepPending = mock(Step.class);
        Step thirdStepNormal = mock(Step.class);
        Step fourthStepAlsoPending = mock(Step.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(firstStepNormal, secondStepPending, thirdStepNormal, fourthStepAlsoPending));
        when(firstStepNormal.perform()).thenReturn(successful("Given I succeed"));
        when(secondStepPending.perform()).thenReturn(pending("When I am pending"));
        when(thirdStepNormal.doNotPerform()).thenReturn(notPerformed("Then I should not be performed"));
        when(fourthStepAlsoPending.doNotPerform()).thenReturn(
        		notPerformed("Then I should not be performed either"));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector), asList(mySteps), story, new MetaFilter());

        // Then
        verify(firstStepNormal).perform();
        verify(secondStepPending).perform();
        verify(thirdStepNormal).doNotPerform();
        verify(fourthStepAlsoPending).doNotPerform();

        verify(reporter).successful("Given I succeed");
        verify(reporter).pending("When I am pending");
        verify(reporter).notPerformed("Then I should not be performed");
        verify(reporter).notPerformed("Then I should not be performed either");
    }

    @Test
    public void shouldReportAnyThrowablesThenHandleAfterStoryIsFinished() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepExceptional = mock(Step.class);
        Step secondStepNotPerformed = mock(Step.class);
        StepResult failure = failed("When I fail", new IllegalStateException());
        StepResult notPerformed = notPerformed("Then I should not be performed");
        when(firstStepExceptional.perform()).thenReturn(failure);
        when(secondStepNotPerformed.doNotPerform()).thenReturn(notPerformed);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(firstStepExceptional, secondStepNotPerformed));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, failureStrategy), asList(mySteps), story, new MetaFilter());

        // Then
        verify(firstStepExceptional).perform();
        verify(secondStepNotPerformed).doNotPerform();

        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory((Story) anyObject(), eq(false));
        inOrder.verify(reporter).beforeScenario((String) anyObject());
        inOrder.verify(reporter).failed("When I fail", failure.getFailure());
        inOrder.verify(reporter).notPerformed("Then I should not be performed");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(false);
        inOrder.verify(failureStrategy).handleFailure(failure.getFailure());
    }

    @Test
    public void shouldResetStateForEachSetOfSteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        Step secondStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(pending("pendingStep"));
        when(secondStep.perform()).thenReturn(successful("secondStep"));
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();    
        Scenario scenario1 = new Scenario();
        Scenario scenario2 = new Scenario();
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, tableRow)).thenReturn(asList(pendingStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, tableRow)).thenReturn(asList(secondStep));
        Story story = new Story(asList(scenario1, scenario2));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);


        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector), asList(mySteps), story, new MetaFilter());

        // Then
        verify(pendingStep).perform();
        verify(secondStep).perform();
        verify(secondStep, never()).doNotPerform();
    }

    @Test
    public void shouldRunBeforeAndAfterStorySteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step beforeStep = mock(Step.class);
        Step afterStep = mock(Step.class);
        when(beforeStep.perform()).thenReturn(successful("beforeStep"));
        when(afterStep.perform()).thenReturn(successful("secondStep"));
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Story story = new Story();
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.BEFORE, false)).thenReturn(asList(beforeStep));
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.AFTER, false)).thenReturn(asList(afterStep));

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector),asList(mySteps), story, new MetaFilter());

        // Then
        verify(beforeStep).perform();
        verify(afterStep).perform();
    }

    @Test
    public void shouldHandlePendingStepsAccordingToStrategy() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = pending("My step isn't defined!");
        when(pendingStep.perform()).thenReturn(pendingResult);
        PendingStepStrategy strategy = mock(PendingStepStrategy.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(pendingStep));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);


        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWithPendingStrategy(collector, reporter,
                strategy), asList(mySteps), story, new MetaFilter());

        // Then
        verify(strategy).handleFailure(pendingResult.getFailure());
    }

    @Test(expected = PendingStepFound.class)
    public void shouldFailWithFailingUpongPendingStepsStrategy() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = pending("My step isn't defined!");
        when(pendingStep.perform()).thenReturn(pendingResult);
        PendingStepStrategy strategy = new FailingUponPendingStep();
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(pendingStep));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);


        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWithPendingStrategy(collector, reporter,
                strategy), asList(mySteps), story, new MetaFilter());

        // Then ... fail as expected
    }

    @Test
    public void shouldRunScenarioWithExamplesTable() throws Throwable {
        // Given
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        Map<String, String> tableRow = examplesTable.getRow(0);
        List<String> givenStories = asList();
        Scenario scenario1 = new Scenario("my title 1", givenStories, examplesTable, asList("step <one>",
                "step <two>"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1));
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        Step firstStep = mock(Step.class, "step <one>");
        when(firstStep.perform()).thenReturn(successful("step <one>"));
        Step secondStep = mock(Step.class, "step <two>");
        when(secondStep.perform()).thenReturn(successful("step <two>"));
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, tableRow)).thenReturn(
                asList(firstStep, secondStep));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configuration, asList(mySteps), story, new MetaFilter());

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(story, false);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).successful("step <one>");
        inOrder.verify(reporter).successful("step <two>");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(false);
    }


    @Test
    public void shouldRunStepsInDryRunMode() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("my title 1", asList("failingStep",
                "successfulStep"));
        Scenario scenario2 = new Scenario("my title 2", asList("successfulStep"));
        Scenario scenario3 = new Scenario("my title 3", asList("successfulStep",
                "pendingStep"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1,
                scenario2, scenario3));
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        IllegalArgumentException anException = new IllegalArgumentException();
        Step pendingStep = mock(Step.class);
        Step successfulStep = mock(Step.class);
        Step failingStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(pending("pendingStep"));
        when(successfulStep.perform()).thenReturn(successful("successfulStep"));
        when(successfulStep.doNotPerform()).thenReturn(notPerformed("successfulStep"));
        when(failingStep.perform()).thenReturn(failed("failingStep", anException));
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, tableRow)).thenReturn(
                asList(failingStep, successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, tableRow)).thenReturn(asList(successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario3, tableRow)).thenReturn(
                asList(successfulStep, pendingStep));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configuration, asList(mySteps), story, new MetaFilter());

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(story, false);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).failed("failingStep", anException);
        inOrder.verify(reporter).notPerformed("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 2");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 3");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).pending("pendingStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(false);
        inOrder.verify(failureStrategy).handleFailure(anException);

    }

    @Test
    public void shouldNotRunStoriesNotAllowedByFilter() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                Arrays.<Step>asList());
        Meta meta = mock(Meta.class);
        Story story = new Story("", Description.EMPTY, meta, Narrative.EMPTY, asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);
        MetaFilter filter = mock(MetaFilter.class);
        String filterAsString = "-some property";
        
        // When
        StoryRunner runner = new StoryRunner();
        when(filter.allow(meta)).thenReturn(false);
        when(filter.asString()).thenReturn(filterAsString);
        runner.run(configurationWith(reporter, collector), asList(mySteps), story, filter);

        // Then
        verify(reporter, never()).beforeStory(story, false);
        verify(reporter).storyNotAllowed(story, filterAsString);
    }
    
    @Test
    public void shouldNotRunScenariosNotAllowedByFilter() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                Arrays.<Step>asList());
        Meta meta = mock(Meta.class);
        Meta storyMeta = mock(Meta.class);
        Story story = new Story("", Description.EMPTY, storyMeta, Narrative.EMPTY, asList(new Scenario("", meta, asList(""), new ExamplesTable(""), asList(""))));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);
        MetaFilter filter = mock(MetaFilter.class);
        String filterAsString = "-some property";

        // When
        StoryRunner runner = new StoryRunner();
        when(filter.allow(storyMeta)).thenReturn(true);
        when(filter.allow(Meta.inherit(meta, storyMeta))).thenReturn(false);
        when(filter.asString()).thenReturn(filterAsString);
        runner.run(configurationWith(reporter, collector), asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, false);
        verify(reporter, never()).beforeScenario("");
        verify(reporter).scenarioNotAllowed(story.getScenarios().get(0), filterAsString);
    }
    
    private void givenStoryWithNoBeforeOrAfterSteps(Story story, boolean givenStory, StepCollector collector, CandidateSteps mySteps) {
        List<Step> steps = asList();
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.BEFORE, givenStory)).thenReturn(steps);
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.AFTER, givenStory)).thenReturn(steps);
    }

    private Configuration configurationWithPendingStrategy(StepCollector collector, StoryReporter reporter,
                                                                PendingStepStrategy strategy) {
        return configurationWith(new RegexStoryParser(), new LoadFromClasspath(), reporter, collector,
                new RethrowingFailure(), strategy);
    }

    private Configuration configurationWith(final StoryReporter reporter, final StepCollector collector) {
        return configurationWith(reporter, collector, new RethrowingFailure());
    }

    private Configuration configurationWith(StoryReporter reporter, StepCollector collector, FailureStrategy failureStrategy) {
        return configurationWith(new RegexStoryParser(), new LoadFromClasspath(), reporter, collector, failureStrategy);
    }

    private Configuration configurationWith(StoryParser parser, final StoryLoader storyLoader, final StoryReporter reporter,
                                                 final StepCollector collector, final FailureStrategy failureStrategy) {
        return configurationWith(parser, storyLoader, reporter, collector, failureStrategy, new PassingUponPendingStep());
    }

    private Configuration configurationWith(final StoryParser parser, final StoryLoader loader, final StoryReporter reporter,
                                                 final StepCollector collector, final FailureStrategy failureStrategy, final PendingStepStrategy pendingStrategy) {

        return new MostUsefulConfiguration()
            .useStoryParser(parser)
            .useStoryLoader(loader)
            .useStepCollector(collector)
            .useDefaultStoryReporter(reporter)
            .useFailureStrategy(failureStrategy)
            .usePendingStepStrategy(pendingStrategy);
    }

}
