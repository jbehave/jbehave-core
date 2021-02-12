package org.jbehave.core.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JUnit4StoryReporterBehaviour {

    private static final String NAME_SCENARIO = "scenario";
    private static final String NAME_STORY = "story";

    @Mock
    private RunNotifier notifier;
    private Description rootDescription = Description.createSuiteDescription("root");
    private Description storyDescription = Description.createSuiteDescription(NAME_STORY);
    private final Description scenarioDescription = Description.createTestDescription(getClass(), NAME_SCENARIO);
    private final Story story = new Story();
    private JUnit4StoryReporter reporter;
    private final Keywords keywords = new Keywords();

    @BeforeEach
    void beforeEach() {
        rootDescription.addChild(storyDescription);
        storyDescription.addChild(scenarioDescription);
        story.namedAs(NAME_STORY);
    }

    @Test
    void shouldCopeWithDescriptionNamesWhenSimilarButForExtraCharacters() {

        Description child1 = addTestToScenario("child");
        Description child2 = addTestToScenario("child.");
        Description child3 = addTestToScenario("child..");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportStepSuccess(reporter);
        reportStepSuccess(reporter);
        reportStepSuccess(reporter);
        verifyStoryStarted();
        verifyScenarioStarted();
        verifyStepSuccess(child1);
        verifyStepSuccess(child2);
        verifyStepSuccess(child3);
    }

    @Test
    void shouldHandleFailedSteps() {

        Description child1 = addTestToScenario("child");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportStepFailure(reporter);
        verifyStoryStarted();
        verifyScenarioStarted();
        verify(notifier).fireTestStarted(child1);
        verify(notifier).fireTestFailure(ArgumentMatchers.any());
        verify(notifier).fireTestFinished(child1);
    }

    @Test
    void shouldHandleIgnorableSteps() {
        Description comment = addTestToScenario("!-- Comment");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        verifyStoryStarted();
        verifyScenarioStarted();
        reportIgnorable(reporter);
        verify(notifier).fireTestIgnored(comment);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);
        verifyTestFinish();
    }

    @Test
    void failureInBeforeStoriesShouldCountOnce() {
        Description beforeStories = addBeforeStories();

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reporter.beforeStoriesSteps(StepCollector.Stage.BEFORE);
        verifyTestRunStarted();

        reporter.failed("BeforeStories", new RuntimeException("..."));

        reporter.afterStoriesSteps(StepCollector.Stage.BEFORE);
        verify(notifier).fireTestStarted(beforeStories);
        verify(notifier).fireTestFailure(Mockito.any());
        // Story, its scenario(s) and its step(s) should not start nor finish if 'before stories' failed.
    }

    @Test
    void shouldNotifyAboutBeforeAndAfterStories() {
        Description beforeStories = addBeforeStories();
        Description child = addTestToScenario("child");
        Description afterStories = addAfterStories();

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reporter.beforeStoriesSteps(StepCollector.Stage.BEFORE);
        verifyTestRunStarted();

        reporter.afterStoriesSteps(StepCollector.Stage.BEFORE);
        verifyStepSuccess(beforeStories);

        reportBefore();
        verifyStoryStarted();
        verifyScenarioStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(child);

        reportScenarioFinish(reporter);
        verifyScenarioFinished();

        reportStoryFinish(reporter);
        verifyStoryFinished();

        reporter.beforeStoriesSteps(StepCollector.Stage.AFTER);
        verify(notifier).fireTestStarted(afterStories);

        reporter.afterStoriesSteps(StepCollector.Stage.AFTER);
        verify(notifier).fireTestFinished(afterStories);
        verifyTestRunFinished();
    }

    @Test
    void shouldNotifyAboutLifecycleScenarioSteps() {
        Description before = addTestToScenario("before scenario step");
        Description child = addTestToScenario("scenario step");
        Description after = addTestToScenario("after scenario step");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        verifyStoryStarted();
        verifyScenarioStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(before);
        reportStepSuccess(reporter);
        verifyStepSuccess(child);
        reportStepSuccess(reporter);
        verifyStepSuccess(after);

        reportScenarioFinish(reporter);
        verifyScenarioFinished();

        reportStoryFinish(reporter);
        verifyStoryFinished();
    }

    @Test
    void shouldNotifyAboutLifecycleStorySteps() {
        storyDescription = storyDescription.childlessCopy();

        rootDescription = rootDescription.childlessCopy();
        rootDescription.addChild(storyDescription);

        Description beforeStoryStep1 = addTestToStory("before story step1");
        Description beforeStoryStep2 = addTestToStory("before story step2");
        storyDescription.addChild(scenarioDescription);
        Description scenarioStep = addTestToScenario("scenario step");
        Description afterStoryStep1 = addTestToStory("after story step1");
        Description afterStoryStep2 = addTestToStory("after story step2");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBeforeStory(story, false);
        verifyStoryStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(beforeStoryStep1);
        reportStepSuccess(reporter);
        verifyStepSuccess(beforeStoryStep2);

        reportBeforeScenario(NAME_SCENARIO);
        verifyScenarioStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(scenarioStep);

        reportScenarioFinish(reporter);
        verifyScenarioFinished();

        reportStepSuccess(reporter);
        verifyStepSuccess(afterStoryStep1);
        reportStepSuccess(reporter);
        verifyStepSuccess(afterStoryStep2);

        reportStoryFinish(reporter);
        verifyStoryFinished();
    }

    @Test
    void shouldNotifyAboutCompositeLifecycleStorySteps() {
        storyDescription = storyDescription.childlessCopy();

        rootDescription = rootDescription.childlessCopy();
        rootDescription.addChild(storyDescription);

        addTestToStory("@BeforeStory");
        Description beforeStoryStep = addTestToStory("before story step");
        Description composedBefore = Description.createTestDescription(this.getClass(), "composedBefore");
        beforeStoryStep.addChild(composedBefore);
        storyDescription.addChild(scenarioDescription);
        Description scenarioStep = addTestToScenario("scenario step");
        Description afterStoryStep = addTestToStory("after story step");
        Description composedAfter = Description.createTestDescription(this.getClass(), "composedAfter");
        Description composedComposedAfter = Description.createTestDescription(this.getClass(), "composedComposedAfter");
        composedAfter.addChild(composedComposedAfter);
        afterStoryStep.addChild(composedAfter);
        addTestToStory("@AfterStory");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBeforeStory(story, false);
        verifyStoryStarted();

        reporter.beforeStep(beforeStoryStep.getDisplayName());
        reportSuccessfulStep(composedBefore);
        reporter.successful(beforeStoryStep.getDisplayName());
        verifyStepSuccess(beforeStoryStep);

        reportBeforeScenario(NAME_SCENARIO);
        verifyScenarioStarted();

        reportSuccessfulStep(scenarioStep);

        reportScenarioFinish(reporter);
        verifyScenarioFinished();

        reporter.beforeStep(afterStoryStep.getDisplayName());
        reporter.beforeStep(composedAfter.getDisplayName());
        reportSuccessfulStep(composedComposedAfter);
        reporter.successful(composedAfter.getDisplayName());
        verifyStepSuccess(composedAfter);
        reporter.successful(afterStoryStep.getDisplayName());
        verifyStepSuccess(afterStoryStep);

        reportStoryFinish(reporter);
        verifyStoryFinished();
    }

    @Test
    void shouldNotNotifySubStepWithoutBeforeAction() {
        scenarioDescription.addChild(Description.TEST_MECHANISM);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportStepSuccess(reporter);
        verifyNoInteractions(notifier);
    }

    @Test
    void shouldNotifyGivenStory() {

        Description givenStoryDescription = addChildGivenStoryToScenario();
        Description child = addTestToScenario("child");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        Story givenStory = new Story();
        givenStory.namedAs("aGivenStory");

        reportBefore();
        reportGivenStoryEvents();
        reportStepSuccess(reporter);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyTestStart();
        verifyStepSuccess(givenStoryDescription);
        verifyStepSuccess(child);
        verifyTestFinish();
    }

    @Test
    void shouldHandleGivenStoryWithExample() {

        Description givenStoryDescription = addChildGivenStoryToScenario();
        Description child = addTestToScenario("child");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportGivenStoryEventsWithExample();
        reportStepSuccess(reporter);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyTestStart();
        verifyStepSuccess(givenStoryDescription);
        verifyStepSuccess(child);
        verifyTestFinish();
    }

    @Test
    void shouldNotifyCompositeSteps() {
        // one story, one scenario, one step, two composite steps
        Description child = addTestToScenario("child");
        Description comp1 = Description.createTestDescription(this.getClass(), "comp1");
        child.addChild(comp1);
        Description comp2 = Description.createTestDescription(this.getClass(), "comp2");
        child.addChild(comp2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.beforeStep("child");
        reporter.successful("child");
        reporter.beforeStep("comp1");
        reporter.successful("comp1");
        reporter.beforeStep("comp2");
        reporter.successful("comp2");
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyTestStart();
        verify(notifier).fireTestStarted(child);
        verifyStepSuccess(comp1);
        verifyStepSuccess(comp2);
        verify(notifier).fireTestFinished(child);
        verifyTestFinish();
    }

    @Test
    void shouldPrepareExampleStepsBeforeScenario() {
        // one story, one scenario, one example, one step,
        Description example = addTestToScenario(keywords.examplesTableRow() + " " + "row");
        Description step = Description.createTestDescription(this.getClass(), "Step");
        example.addChild(step);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.example(null, 0);
        reportStepSuccess(reporter);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyTestStart();
        verifyStepSuccess(step);
        verifyTestFinish();
    }

    @Test
    void shouldHandleExampleStepsInCombinationWithCompositeSteps() {
        // one story, one scenario, one example, one composite step of 2 steps
        Description example = addTestToScenario(keywords.examplesTableRow() + " " + "row");
        Description step = Description.createTestDescription(this.getClass(), "Step");
        example.addChild(step);

        Description comp1 = Description.createTestDescription(this.getClass(), "comp1");
        step.addChild(comp1);
        Description comp2 = Description.createTestDescription(this.getClass(), "comp2");
        step.addChild(comp2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.example(null, 0);
        reporter.beforeStep("child");
        reporter.successful("child");
        reporter.beforeStep("comp1");
        reporter.successful("comp1");
        reporter.beforeStep("comp2");
        reporter.successful("comp2");
        reportStepSuccess(reporter);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyStoryStarted();
        verifyScenarioStarted();
        verifyTestStart();
        verify(notifier).fireTestStarted(step);
        verifyStepSuccess(comp1);
        verifyStepSuccess(comp2);
        verify(notifier).fireTestFinished(step);
        verifyTestFinish();
    }

    @Test
    void shouldHandleExampleStepsInCombinationWithGivenStories() {
        // one story, one scenario, one given story, one example, one step
        Description givenStoryDescription = addChildGivenStoryToScenario();
        // one story, one scenario, one example, one step,
        Description example = addTestToScenario(keywords.examplesTableRow() + " " + "row");
        Description step = Description.createTestDescription(this.getClass(), "Step");
        example.addChild(step);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportGivenStoryEvents();
        reporter.example(null, 0);
        reportStepSuccess(reporter);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyStoryStarted();
        verifyScenarioStarted();
        verifyStepSuccess(givenStoryDescription);
        verifyStepSuccess(step);
        verifyTestFinish();
    }

    @Test
    void shouldFailForPendingStepsAtBothStepAndScenarioLevelsIfConfigurationSaysSo() {
        Description child1 = addTestToScenario("child1");
        Description child2 = addTestToScenario("child2");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        PendingStepStrategy strategy = new FailingUponPendingStep();
        reporter.usePendingStepStrategy(strategy);

        reportBefore();
        reporter.pending("child1");
        reporter.failed("child2", new UUIDExceptionWrapper(new Exception("FAIL")));
        verifyStoryStarted();
        verifyScenarioStarted();
        verify(notifier).fireTestStarted(child1);
        verify(notifier, times(2)).fireTestFailure(Mockito.any());
        verify(notifier, times(1)).fireTestFinished(child1);
        verify(notifier, times(1)).fireTestFinished(child2);
    }

    @Test
    void shouldIgnorePendingStepsIfConfigurationSaysSo() {
        Description child = addTestToScenario("child");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.pending("child");
        verifyStoryStarted();
        verifyScenarioStarted();
        verify(notifier, VerificationModeFactory.times(0)).fireTestStarted(
                child);
        verify(notifier).fireTestIgnored(child);
    }

    @Test
    void shouldHandleFailuresInBeforeStories() {
        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBeforeStory(story, false);
        reporter.failed(NAME_STORY, new UUIDExceptionWrapper("Error Message",
                new RuntimeException("Cause")));
        ArgumentCaptor<Failure> argument = ArgumentCaptor
                .forClass(Failure.class);
        verify(notifier).fireTestFailure(argument.capture());
        assertThat(argument.getValue().getDescription(), is(storyDescription));
    }

    @Test
    void shouldNotifyAboutNotAllowedScenario() {
        Description child1 = addTestToScenario("child");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.scenarioExcluded(mock(Scenario.class), "filter");
        verifyStoryStarted();
        verifyScenarioStarted();
        verify(notifier).fireTestIgnored(child1);
        verify(notifier).fireTestIgnored(scenarioDescription);
    }

    private void reportStoryFinish(JUnit4StoryReporter reporter) {
        reporter.afterStory(false);
    }

    private void reportScenarioFinish(JUnit4StoryReporter reporter) {
        reporter.afterScenario(mock(Timing.class));
        // test should not be finished until we send the final event
        verify(notifier, VerificationModeFactory.times(0)).fireTestRunFinished(
                Mockito.any());
    }

    private void reportSuccessfulStep(Description step) {
        reporter.beforeStep(step.getDisplayName());
        reporter.successful(step.getDisplayName());
        verifyStepSuccess(step);
    }

    private void reportStepSuccess(JUnit4StoryReporter reporter) {
        reporter.beforeStep("child");
        reporter.successful("child");
    }

    private void reportStepFailure(JUnit4StoryReporter reporter) {
        reporter.beforeStep("child");
        reporter.failed("child", new UUIDExceptionWrapper(new Exception("FAIL")));
    }

    private void reportIgnorable(JUnit4StoryReporter reporter) {
        reporter.ignorable("!-- Comment");
    }

    private void reportGivenStoryEvents() {
        Story givenStory = createGivenStory();

        // Begin Given Story
        reportBeforeStory(givenStory, true);
        reportBeforeScenario("givenScenario");
        reporter.beforeStep("givenStep");
        reporter.successful("givenStep");
        reportAfter();
        // End Given Story
    }

    private void reportAfter() {
        reporter.afterScenario(mock(Timing.class));
        reporter.afterStory(true);
    }

    private Story createGivenStory() {
        Story givenStory = new Story();
        givenStory.namedAs("aGivenStory");
        return givenStory;
    }

    private void reportGivenStoryEventsWithExample() {
        Story givenStory = createGivenStory();

        // Begin Given Story
        reportBeforeStory(givenStory, true);
        reportBeforeScenario("givenScenario");
        reporter.example(Collections.singletonMap("givenKey", "givenValue"), 0);
        reporter.beforeStep("givenStep");
        reporter.successful("givenStep");
        reportAfter();
        // End Given Story
    }

    private void verifyTestFinish() {
        verifyScenarioFinished();
        verifyStoryFinished();
    }

    private void verifyTestStart() {
        verifyStoryStarted();
        verifyScenarioStarted();
    }

    private void verifyStoryFinished() {
        verify(notifier).fireTestFinished(storyDescription);
    }

    private void verifyScenarioFinished() {
        verify(notifier).fireTestFinished(scenarioDescription);
    }

    private void verifyScenarioStarted() {
        verify(notifier).fireTestStarted(scenarioDescription);
    }

    private void verifyStoryStarted() {
        verify(notifier).fireTestStarted(storyDescription);
    }

    private void verifyStepSuccess(Description step) {
        verify(notifier).fireTestStarted(step);
        verify(notifier).fireTestFinished(step);
    }

    private void verifyTestRunFinished() {
        verify(notifier).fireTestRunFinished(ArgumentMatchers.any());
    }

    private void verifyTestRunStarted() {
        verify(notifier).fireTestRunStarted(ArgumentMatchers.any());
    }

    private Description addTestToStory(String childName) {
        Description child = Description.createTestDescription(this.getClass(), childName);
        storyDescription.addChild(child);
        return child;
    }

    private Description addTestToScenario(String childName) {
        Description child = Description.createTestDescription(this.getClass(), childName);
        scenarioDescription.addChild(child);
        return child;
    }

    private Description addChildGivenStoryToScenario() {
        Description givenStoryDescription = Description.createSuiteDescription("aGivenStory");
        scenarioDescription.addChild(givenStoryDescription);
        return givenStoryDescription;
    }

    private Description addBeforeStories() {
        Description beforeStories = Description.createTestDescription(Object.class, "BeforeStories");
        rootDescription.addChild(beforeStories);
        return beforeStories;
    }

    private Description addAfterStories() {
        Description afterStories = Description.createTestDescription(Object.class, "AfterStories");
        rootDescription.addChild(afterStories);
        return afterStories;
    }

    private void reportBefore() {
        reportBeforeStory(story, false);
        reportBeforeScenario(scenarioDescription.getDisplayName());
    }

    private void reportBeforeStory(Story story, boolean givenStory) {
        reporter.beforeStory(story, givenStory);
    }

    private void reportBeforeScenario(String scenarioTitle) {
        reporter.beforeScenario(new Scenario(scenarioTitle, Meta.EMPTY));
    }
}
