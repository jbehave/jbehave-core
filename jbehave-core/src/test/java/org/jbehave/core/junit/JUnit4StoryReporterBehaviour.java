package org.jbehave.core.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.annotation.Annotation;
import java.util.Collections;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.junit.JUnit4DescriptionGenerator.JUnit4Test;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
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
    private static final String ROOT = "root";
    private static final String BEFORE_STORIES = "BeforeStories";
    private static final String AFTER_STORIES = "AfterStories";

    @Mock
    private RunNotifier notifier;
    private final JUnit4Test junitTestMeta = new JUnit4Test() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return JUnit4Test.class;
        }
    };
    private Description rootDescription = Description.createSuiteDescription(ROOT);
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

        Description step1 = createTest("child");
        Description step2 = createTest("child.");
        Description step3 = createTest("child..");

        scenarioDescription.addChild(step1);
        scenarioDescription.addChild(step2);
        scenarioDescription.addChild(step3);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportStepSuccess(reporter);
        reportStepSuccess(reporter);
        reportStepSuccess(reporter);
        verifyStoryStarted();
        verifyScenarioStarted();
        verifyStepSuccess(step1);
        verifyStepSuccess(step2);
        verifyStepSuccess(step3);
    }

    @Test
    void shouldHandleFailedSteps() {

        Description step = createTest("child");

        scenarioDescription.addChild(step);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportStepFailure(reporter);
        verifyStoryStarted();
        verifyScenarioStarted();
        verify(notifier).fireTestStarted(step);
        verify(notifier).fireTestFailure(ArgumentMatchers.any());
        verify(notifier).fireTestFinished(step);
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
        Description beforeStories = Description.createTestDescription(Object.class, BEFORE_STORIES);
        rootDescription.addChild(beforeStories);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reporter.beforeStoriesSteps(StepCollector.Stage.BEFORE);
        verifyTestRunStarted();

        reporter.failed(BEFORE_STORIES, new RuntimeException("..."));

        reporter.afterStoriesSteps(StepCollector.Stage.BEFORE);
        verify(notifier).fireTestStarted(beforeStories);
        verify(notifier).fireTestFailure(Mockito.any());
        // Story, its scenario(s) and its step(s) should not start nor finish if 'before stories' failed.
    }

    @Test
    void shouldNotifyAboutBeforeAndAfterStories() {
        Description beforeStories = Description.createTestDescription(Object.class, BEFORE_STORIES);
        rootDescription.addChild(beforeStories);

        Description step = createTest("child");
        scenarioDescription.addChild(step);

        Description afterStories = Description.createTestDescription(Object.class, AFTER_STORIES);
        rootDescription.addChild(afterStories);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reporter.beforeStoriesSteps(StepCollector.Stage.BEFORE);
        verifyTestRunStarted();

        reporter.afterStoriesSteps(StepCollector.Stage.BEFORE);
        verifyStepSuccess(beforeStories);

        reportBefore();
        verifyStoryStarted();
        verifyScenarioStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(step);

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
    void shouldNotifyBeforeAndAfterStoriesInEmptySuite() {
        rootDescription = Description.createSuiteDescription(ROOT);

        Description beforeStories = Description.createTestDescription(Object.class, BEFORE_STORIES);
        rootDescription.addChild(beforeStories);

        Description afterStories = Description.createTestDescription(Object.class, AFTER_STORIES);
        rootDescription.addChild(afterStories);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reporter.beforeStoriesSteps(StepCollector.Stage.BEFORE);
        verifyTestRunStarted();

        reportStepSuccess(reporter, "beforeStoriesStep");

        reporter.afterStoriesSteps(StepCollector.Stage.BEFORE);
        verifyStepSuccess(beforeStories);

        reporter.beforeStoriesSteps(StepCollector.Stage.AFTER);
        verify(notifier).fireTestStarted(afterStories);

        reportStepSuccess(reporter, "afterStoriesStep");

        reporter.afterStoriesSteps(StepCollector.Stage.AFTER);
        verify(notifier).fireTestFinished(afterStories);
        verifyTestRunFinished();
    }

    @Test
    void shouldNotifyAboutLifecycleScenarioSteps() {
        Description beforeScenarioStep = createTest("before scenario step");
        Description scenarioStep = createTest("scenario step");
        Description afterScenarioStep = createTest("after scenario step");

        scenarioDescription.addChild(beforeScenarioStep);
        scenarioDescription.addChild(scenarioStep);
        scenarioDescription.addChild(afterScenarioStep);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        verifyStoryStarted();
        verifyScenarioStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(beforeScenarioStep);
        reportStepSuccess(reporter);
        verifyStepSuccess(scenarioStep);
        reportStepSuccess(reporter);
        verifyStepSuccess(afterScenarioStep);

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

        Description beforeStoryStep1 = createTest("before story step1");
        Description beforeStoryStep2 = createTest("before story step2");
        storyDescription.addChild(beforeStoryStep1);
        storyDescription.addChild(beforeStoryStep2);

        storyDescription.addChild(scenarioDescription);
        Description scenarioStep = createTest("scenario step");
        scenarioDescription.addChild(scenarioStep);

        Description afterStoryStep1 = createTest("after story step1");
        Description afterStoryStep2 = createTest("after story step2");
        storyDescription.addChild(afterStoryStep1);
        storyDescription.addChild(afterStoryStep2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBeforeStory(story, false);
        verifyStoryStarted();

        performStorySteps(Stage.BEFORE, ExecutionType.SYSTEM, () -> { });

        performStorySteps(Stage.BEFORE, ExecutionType.USER, () -> {
            reportStepSuccess(reporter);
            verifyStepSuccess(beforeStoryStep1);
            reportStepSuccess(reporter);
            verifyStepSuccess(beforeStoryStep2);
        });

        reportBeforeScenario(NAME_SCENARIO);
        verifyScenarioStarted();

        reportStepSuccess(reporter);
        verifyStepSuccess(scenarioStep);

        reportScenarioFinish(reporter);
        verifyScenarioFinished();

        performStorySteps(Stage.AFTER, ExecutionType.USER, () -> {
            reportStepSuccess(reporter);
            verifyStepSuccess(afterStoryStep1);
            reportStepSuccess(reporter);
            verifyStepSuccess(afterStoryStep2);
        });

        performStorySteps(Stage.AFTER, ExecutionType.SYSTEM, () -> { });

        reportStoryFinish(reporter);
        verifyStoryFinished();
    }

    @Test
    void shouldNotifyAboutCompositeLifecycleStorySteps() {
        storyDescription = storyDescription.childlessCopy();

        rootDescription = rootDescription.childlessCopy();
        rootDescription.addChild(storyDescription);

        Description beforeStorySystemStep1 = createTest("@BeforeStory 1");
        storyDescription.addChild(beforeStorySystemStep1);
        Description beforeStorySystemStep2 = createTest("@BeforeStory 2");
        storyDescription.addChild(beforeStorySystemStep2);
        Description beforeStoryStep = createTest("before story step");
        storyDescription.addChild(beforeStoryStep);

        Description composedBefore = createTest("composedBefore");
        beforeStoryStep.addChild(composedBefore);
        storyDescription.addChild(scenarioDescription);
        Description scenarioStep = createTest("scenario step");
        scenarioDescription.addChild(scenarioStep);
        Description afterStoryStep = createTest("after story step");
        storyDescription.addChild(afterStoryStep);
        Description composedAfter = createTest("composedAfter");
        Description composedComposedAfter = createTest("composedComposedAfter");
        composedAfter.addChild(composedComposedAfter);
        afterStoryStep.addChild(composedAfter);
        Description afterStorySystemStep1 = createTest("@AfterStory 1");
        storyDescription.addChild(afterStorySystemStep1);
        Description afterStorySystemStep2 = createTest("@AfterStory 2");
        storyDescription.addChild(afterStorySystemStep2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBeforeStory(story, false);
        verifyStoryStarted();

        performStorySteps(Stage.BEFORE, ExecutionType.SYSTEM, () -> {
            reportSuccessfulStep(beforeStorySystemStep1);
            reportSuccessfulStep(beforeStorySystemStep2);
        });

        performStorySteps(Stage.BEFORE, ExecutionType.USER, () -> {
            reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, beforeStoryStep.getDisplayName()));
            reportSuccessfulStep(composedBefore);
            reporter.successful(beforeStoryStep.getDisplayName());
            verifyStepSuccess(beforeStoryStep);
        });

        reportBeforeScenario(NAME_SCENARIO);
        verifyScenarioStarted();

        reportSuccessfulStep(scenarioStep);

        reportScenarioFinish(reporter);
        verifyScenarioFinished();

        performStorySteps(Stage.AFTER, ExecutionType.USER, () -> {
            reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, afterStoryStep.getDisplayName()));
            reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, composedAfter.getDisplayName()));
            reportSuccessfulStep(composedComposedAfter);
            reporter.successful(composedAfter.getDisplayName());
            verifyStepSuccess(composedAfter);
            reporter.successful(afterStoryStep.getDisplayName());
            verifyStepSuccess(afterStoryStep);
        });

        performStorySteps(Stage.AFTER, ExecutionType.SYSTEM, () -> {
            reportSuccessfulStep(afterStorySystemStep1);
            reportSuccessfulStep(afterStorySystemStep2);
        });

        reportStoryFinish(reporter);
        verifyStoryFinished();
    }

    private void performStorySteps(Stage stage, Lifecycle.ExecutionType type, Runnable steps) {
        reporter.beforeStorySteps(stage, type);
        steps.run();
        reporter.afterStorySteps(stage, type);
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

        Description givenStoryDescription = Description.createSuiteDescription("aGivenStory");
        Description step = createTest("child");

        scenarioDescription.addChild(givenStoryDescription);
        scenarioDescription.addChild(step);

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
        verifyStepSuccess(step);
        verifyTestFinish();
    }

    @Test
    void shouldHandleGivenStoryWithExample() {

        Description givenStoryDescription = Description.createSuiteDescription("aGivenStory");
        Description step = createTest("child");

        scenarioDescription.addChild(givenStoryDescription);
        scenarioDescription.addChild(step);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportGivenStoryEventsWithExample();
        reportStepSuccess(reporter);
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyTestStart();
        verifyStepSuccess(givenStoryDescription);
        verifyStepSuccess(step);
        verifyTestFinish();
    }

    @Test
    void shouldNotifyCompositeSteps() {
        // one story, one scenario, one step, two composite steps
        Description child = addTestToScenario("child");
        Description comp1 = createTest("comp1");
        child.addChild(comp1);
        Description comp2 = createTest("comp2");
        child.addChild(comp2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reportStepSuccess(reporter, "child");
        reportStepSuccess(reporter, "comp1");
        reportStepSuccess(reporter, "comp2");
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
        Description step = createTest("Step");
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
        Description compositeStep = createTest("Step");
        example.addChild(compositeStep);

        Description comp1 = createTest("comp1");
        compositeStep.addChild(comp1);
        Description comp2 = createTest("comp2");
        compositeStep.addChild(comp2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.example(null, 0);
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, "child"));
        reportStepSuccess(reporter, "comp1");
        reportStepSuccess(reporter, "comp2");
        reporter.successful("child");
        reportScenarioFinish(reporter);
        reportStoryFinish(reporter);

        verifyStoryStarted();
        verifyScenarioStarted();
        verifyTestStart();
        verify(notifier).fireTestStarted(compositeStep);
        verifyStepSuccess(comp1);
        verifyStepSuccess(comp2);
        verify(notifier).fireTestFinished(compositeStep);
        verifyTestFinish();
    }

    @Test
    void shouldHandleExampleStepsInCombinationWithGivenStories() {
        // one story, one scenario, one given story, one example, one step
        Description givenStoryDescription = Description.createSuiteDescription("aGivenStory");
        scenarioDescription.addChild(givenStoryDescription);
        // one story, one scenario, one example, one step,
        Description example = addTestToScenario(keywords.examplesTableRow() + " " + "row");
        Description step = createTest("Step");
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
        Description step1 = createTest("child1");
        Description step2 = createTest("child2");

        scenarioDescription.addChild(step1);
        scenarioDescription.addChild(step2);

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        PendingStepStrategy strategy = new FailingUponPendingStep();
        reporter.usePendingStepStrategy(strategy);

        reportBefore();
        reporter.pending((StepCreator.PendingStep) StepCreator.createPendingStep("child1", null));
        reporter.failed("child2", new UUIDExceptionWrapper(new Exception("FAIL")));
        verifyStoryStarted();
        verifyScenarioStarted();
        verify(notifier).fireTestStarted(step1);
        verify(notifier, times(2)).fireTestFailure(Mockito.any());
        verify(notifier, times(1)).fireTestFinished(step1);
        verify(notifier, times(1)).fireTestFinished(step2);
    }

    @Test
    void shouldIgnorePendingStepsIfConfigurationSaysSo() {
        Description child = addTestToScenario("child");

        reporter = new JUnit4StoryReporter(notifier, rootDescription, keywords);

        reportBefore();
        reporter.pending((StepCreator.PendingStep) StepCreator.createPendingStep("child1", null));
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
        reportStepSuccess(reporter, step.getDisplayName());
        verifyStepSuccess(step);
    }

    private void reportStepSuccess(JUnit4StoryReporter reporter) {
        reportStepSuccess(reporter, "child");
    }

    private void reportStepSuccess(JUnit4StoryReporter reporter, String step) {
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, step));
        reporter.successful(step);
    }

    private void reportStepFailure(JUnit4StoryReporter reporter) {
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, "child"));
        reporter.failed("child", new UUIDExceptionWrapper(new Exception("FAIL")));
    }

    private void reportIgnorable(JUnit4StoryReporter reporter) {
        reporter.beforeStep(new Step(StepExecutionType.IGNORABLE, "!-- Comment"));
        reporter.ignorable("!-- Comment");
    }

    private void reportGivenStoryEvents() {
        Story givenStory = createGivenStory();

        // Begin Given Story
        reportBeforeStory(givenStory, true);
        performStorySteps(Stage.BEFORE, ExecutionType.SYSTEM, () -> { });
        performStorySteps(Stage.BEFORE, ExecutionType.USER, () -> { });
        reportBeforeScenario("givenScenario");
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, "givenStep"));
        reporter.successful("givenStep");
        reportAfter();
        // End Given Story
    }

    private void reportAfter() {
        reporter.afterScenario(mock(Timing.class));
        performStorySteps(Stage.AFTER, ExecutionType.USER, () -> { });
        performStorySteps(Stage.AFTER, ExecutionType.SYSTEM, () -> { });
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
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, "givenStep"));
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

    private Description addTestToScenario(String childName) {
        Description child = createTest(childName);
        scenarioDescription.addChild(child);
        return child;
    }

    private Description createTest(String childName) {
        return Description.createTestDescription(this.getClass(), childName, junitTestMeta);
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
