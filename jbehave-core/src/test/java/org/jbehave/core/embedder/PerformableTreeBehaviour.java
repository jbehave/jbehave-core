package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.jbehave.core.annotations.Scope;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.context.StepsContext;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * @author Valery Yatsynovich
 */
public class PerformableTreeBehaviour {

    private static final String GIVEN_SCENARIO_FAIL = "Scenario: given scenario title\nWhen I fail";
    private static final String STORY_PATH = "path";
    private static final Story EMPTY_STORY = new Story(STORY_PATH, null, null, null, null, Collections.emptyList());
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Test
    public void shouldAddNotAllowedPerformableScenariosToPerformableStory() {
        Scenario scenario = new Scenario("scenario title", Meta.createMeta("@skip", new Keywords()));
        Story story = new Story(STORY_PATH, Collections.singletonList(scenario));
        List<Story> stories = Collections.singletonList(story);

        StepCollector stepCollector = mock(StepCollector.class);
        Configuration configuration = mock(Configuration.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);
        when(configuration.storyControls()).thenReturn(new StoryControls());
        List<CandidateSteps> candidateSteps = Collections.emptyList();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        MetaFilter filter = new MetaFilter("-skip", embedderMonitor);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, filter, failures);
        performableTree.addStories(runContext, stories);

        assertThat(performableTree.getRoot().getStories().get(0).getScenarios().size(), equalTo(1));

        InOrder ordered = inOrder(stepCollector);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(candidateSteps, StepCollector.Stage.BEFORE);
        ordered.verify(stepCollector).collectLifecycleSteps(eq(candidateSteps), eq(story.getLifecycle()),
                any(Meta.class), eq(Scope.STORY), any(MatchingStepMonitor.class));
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(candidateSteps, story, StepCollector.Stage.BEFORE,
                false);
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(candidateSteps, story, StepCollector.Stage.AFTER,
                false);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(candidateSteps, StepCollector.Stage.AFTER);
        verifyNoMoreInteractions(stepCollector);
    }

    @Test
    public void shouldNotSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(false, GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(false));
    }

    @Test
    public void shouldSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(true, GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(true));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    @Test
    public void shouldNotSkipStoryWhenGivenStoryIsPassed() {
        RunContext context = performStoryRun(true, "Scenario: given scenario title");
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    private RunContext performStoryRun(boolean skipScenariosAfterGivenStoriesFailure, String givenStoryAsString) {
        String givenStoryPath = "given/path";
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, new GivenStories(givenStoryPath),
                Collections.singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doSkipStoryIfGivenStoryFailed(skipScenariosAfterGivenStoriesFailure));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(givenStoryPath)).thenReturn(givenStoryAsString);
        List<CandidateSteps> candidateSteps = Collections.emptyList();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, new MetaFilter(), failures);
        performableTree.addStories(runContext, Collections.singletonList(story));
        performableTree.perform(runContext, story);
        return runContext;
    }

    @Test
    public void performStoryIfDryRunTrue() {
        Scenario scenario = new Scenario("scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, null,
                Collections.singletonList(scenario));
        Configuration configuration = mock(Configuration.class);
        when(configuration.stepCollector()).thenReturn(new MarkUnmatchedStepsAsPending());
        when(configuration.storyControls()).thenReturn(new StoryControls());
        when(configuration.stepsContext()).thenReturn(new StepsContext());
        when(configuration.dryRun()).thenReturn(true);
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        StoryReporter storyReporter = mock(StoryReporter.class);
        when(configuration.storyReporter(STORY_PATH)).thenReturn(storyReporter);
        List<CandidateSteps> candidateSteps = Collections.emptyList();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, new MetaFilter(), failures);
        performableTree.addStories(runContext, Collections.singletonList(story));
        performableTree.perform(runContext, story);

        verify(storyReporter).dryRun();
    }

    @Test
    public void shouldNotResetFailuresBetweenStories() {
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story1 = new Story("path1", null, null, null, new GivenStories("given/path1"),
                Collections.singletonList(scenario));
        Story story2 = new Story("path2", null, null, null, new GivenStories("given/path2"),
                Collections.singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doResetStateBeforeScenario(false));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(anyString())).thenReturn(GIVEN_SCENARIO_FAIL);
        List<CandidateSteps> candidateSteps = new InstanceStepsFactory(configuration, new Steps())
                .createCandidateSteps();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = new BatchFailures();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, new MetaFilter(), failures);
        performableTree.addStories(runContext, Arrays.asList(story1, story2));
        performableTree.perform(runContext, story1);
        performableTree.perform(runContext, story2);

        assertThat(failures.size(), is(2));
    }

    @Test
    public void shouldResetFailuresOnReRun() {
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, new GivenStories("given/path"),
                Collections.singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doResetStateBeforeScenario(false));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(anyString())).thenReturn(GIVEN_SCENARIO_FAIL);
        List<CandidateSteps> candidateSteps = new InstanceStepsFactory(configuration, new Steps())
                .createCandidateSteps();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = new BatchFailures();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, new MetaFilter(), failures);
        performableTree.addStories(runContext, Arrays.asList(story));
        performableTree.perform(runContext, story);
        performableTree.perform(runContext, story);

        assertThat(failures.size(), is(1));
    }

    @Test
    public void shouldNotShareStoryStateBetweenThreads() throws Throwable {
        RunContext context = runStoryInContext(EMPTY_STORY);
        assertThat(context.state().getClass().getSimpleName(), is("FineSoFar"));
        assertReturnsNullInAnotherThread(context::state);
    }

    @Test
    public void shouldNotShareStoryPathBetweenThreads() throws Throwable {
        RunContext context = runStoryInContext(EMPTY_STORY);
        assertThat(context.path(), is(STORY_PATH));
        assertReturnsNullInAnotherThread(context::path);
    }

    @Test
    public void shouldNotShareStoryReporterBetweenThreads() throws Throwable {
        RunContext context = runStoryInContext(EMPTY_STORY);
        assertThat(context.reporter(), instanceOf(StoryReporter.class));
        assertReturnsNullInAnotherThread(context::reporter);
    }

    private RunContext runStoryInContext(Story story) {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useStoryLoader(mock(StoryLoader.class));
        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, Collections.emptyList(),
                mock(EmbedderMonitor.class), new MetaFilter(), mock(BatchFailures.class));
        performableTree.addStories(runContext, Arrays.asList(story));
        performableTree.perform(runContext, story);
        return runContext;
    }

    private void assertReturnsNullInAnotherThread(Supplier<Object> supplier) throws Throwable {
        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertNull(supplier.get());
                return null;
            }
        };

        try {
            EXECUTOR.submit(task).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public static class Steps {

        @When("I fail")
        public void fail() {
            throw new RuntimeException();
        }
    }
}
