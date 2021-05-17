package org.jbehave.core.embedder;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.jbehave.core.steps.StepCollector.Stage;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.context.StepsContext;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * @author Valery Yatsynovich
 */
class PerformableTreeBehaviour {

    private static final String GIVEN_SCENARIO_FAIL = "Scenario: given scenario title\nWhen I fail";
    private static final String STORY_PATH = "path";
    private static final Story EMPTY_STORY = new Story(STORY_PATH, null, null, null, null, emptyList());
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Test
    void shouldAddExcludedPerformableScenariosToPerformableStory() {
        Scenario scenario = new Scenario("scenario title", Meta.createMeta("@skip", new Keywords()));
        Meta storyMeta = new Meta();
        Story story = new Story(STORY_PATH, null, storyMeta, null, singletonList(scenario));
        List<Story> stories = singletonList(story);

        StepCollector stepCollector = mock(StepCollector.class);
        Configuration configuration = mock(Configuration.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);
        when(configuration.storyControls()).thenReturn(new StoryControls());
        CandidateSteps candidate = mock(CandidateSteps.class);
        List<BeforeOrAfterStep> beforeStories = singletonList(mock(BeforeOrAfterStep.class));
        when(candidate.listBeforeStories()).thenReturn(beforeStories);
        List<BeforeOrAfterStep> beforeStory = singletonList(mock(BeforeOrAfterStep.class));
        when(candidate.listBeforeStory(false)).thenReturn(beforeStory);
        List<BeforeOrAfterStep> afterStory = singletonList(mock(BeforeOrAfterStep.class));
        when(candidate.listAfterStory(false)).thenReturn(afterStory);
        List<BeforeOrAfterStep> afterStories = singletonList(mock(BeforeOrAfterStep.class));
        when(candidate.listAfterStories()).thenReturn(afterStories);
        List<StepCandidate> stepCandidates = singletonList(mock(StepCandidate.class));
        when(candidate.listCandidates()).thenReturn(stepCandidates);
        List<CandidateSteps> candidateSteps = singletonList(candidate);
        AllStepCandidates allStepCandidates = new AllStepCandidates(candidateSteps);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        MetaFilter filter = new MetaFilter("-skip", embedderMonitor);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates,
                embedderMonitor, filter, failures);
        performableTree.addStories(runContext, stories);

        assertThat(performableTree.getRoot().getStories().get(0).getScenarios().size(), equalTo(1));

        InOrder ordered = inOrder(stepCollector);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(beforeStories);
        ordered.verify(stepCollector).collectLifecycleSteps(eq(stepCandidates), eq(story.getLifecycle()),
                eq(storyMeta), eq(Scope.STORY), any(MatchingStepMonitor.class));
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(beforeStory, storyMeta);
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(afterStory, storyMeta);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(afterStories);
        verifyNoMoreInteractions(stepCollector);
    }

    @Test
    void shouldNotSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(false, GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(false));
    }

    @Test
    void shouldSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(true, GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(true));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    @Test
    void shouldNotSkipStoryWhenGivenStoryIsPassed() {
        RunContext context = performStoryRun(true, "Scenario: given scenario title");
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    @Test
    void shouldReplaceParameters() {
        ParameterControls parameterControls = new ParameterControls();
        PerformableTree performableTree = new PerformableTree();
        Configuration configuration = mock(Configuration.class);
        when(configuration.storyControls()).thenReturn(new StoryControls());
        List<CandidateSteps> candidateSteps = emptyList();
        AllStepCandidates allStepCandidates = new AllStepCandidates(candidateSteps);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);
        PerformableTree.RunContext context = spy(performableTree.newRunContext(configuration, allStepCandidates,
                embedderMonitor, new MetaFilter(), failures));

        StoryControls storyControls = mock(StoryControls.class);
        when(configuration.storyControls()).thenReturn(storyControls);
        when(storyControls.skipBeforeAndAfterScenarioStepsIfGivenStory()).thenReturn(false);
        when(configuration.parameterConverters()).thenReturn(new DefaultParameterConverters());
        when(configuration.parameterControls()).thenReturn(parameterControls);

        GivenStories givenStories = new GivenStories("");

        Map<String,String> scenarioExample = new HashMap<>();
        scenarioExample.put("var1","E");
        scenarioExample.put("var3","<var2>F");

        Map<String,String> scenarioExampleSecond = new HashMap<>();
        scenarioExampleSecond.put("var1","G<var2>");
        scenarioExampleSecond.put("var3","<var2>");
        ExamplesTable scenarioExamplesTable = ExamplesTable.empty().withRows(
                Arrays.asList(scenarioExample, scenarioExampleSecond));

        String scenarioTitle = "scenario title";
        Scenario scenario = new Scenario(scenarioTitle, new Meta(), givenStories, scenarioExamplesTable, emptyList());

        Narrative narrative = mock(Narrative.class);
        Lifecycle lifecycle = mock(Lifecycle.class);
        Story story = new Story(null, null, new Meta(), narrative, givenStories, lifecycle,
                singletonList(scenario));

        ExamplesTable storyExamplesTable = mock(ExamplesTable.class);
        when(lifecycle.getExamplesTable()).thenReturn(storyExamplesTable);
        Map<String,String> storyExampleFirstRow = new HashMap<>();
        storyExampleFirstRow.put("var1","a");
        storyExampleFirstRow.put("var2","b");
        Map<String,String> storyExampleSecondRow = new HashMap<>();
        storyExampleSecondRow.put("var1","c");
        storyExampleSecondRow.put("var2","d");

        when(storyExamplesTable.getRows()).thenReturn(Arrays.asList(storyExampleFirstRow, storyExampleSecondRow));

        Keywords keywords = mock(Keywords.class);
        when(configuration.keywords()).thenReturn(keywords);

        StepMonitor stepMonitor = mock(StepMonitor.class);
        when(configuration.stepMonitor()).thenReturn(stepMonitor);
        StepCollector stepCollector = mock(StepCollector.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);
        Map<Stage, List<Step>> lifecycleSteps = new EnumMap<>(Stage.class);
        lifecycleSteps.put(Stage.BEFORE, emptyList());
        lifecycleSteps.put(Stage.AFTER, emptyList());
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.STORY),
                any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.SCENARIO),
                any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        performableTree.addStories(context, singletonList(story));
        List<PerformableTree.PerformableScenario> performableScenarios = performableTree.getRoot().getStories().get(0)
                .getScenarios();

        assertThat(performableScenarios.size(), is(scenarioExample.size()));
        assertThat(performableScenarios.get(0).getScenario().getTitle(), is(scenarioTitle + " [1]"));
        List<PerformableTree.ExamplePerformableScenario> examplePerformableScenarios = performableScenarios.get(0)
                .getExamples();
        assertThat(examplePerformableScenarios.size(), is(scenarioExample.size()));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var1"), is("E"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var2"), is("b"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var3"), is("bF"));

        assertThat(examplePerformableScenarios.get(1).getParameters().get("var1"), is("Gb"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var2"), is("b"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var3"), is("b"));

        assertThat(performableScenarios.get(1).getScenario().getTitle(), is(scenarioTitle + " [2]"));
        examplePerformableScenarios = performableScenarios.get(1).getExamples();
        assertThat(examplePerformableScenarios.size(), is(scenarioExample.size()));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var1"), is("E"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var2"), is("d"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var3"), is("dF"));

        assertThat(examplePerformableScenarios.get(1).getParameters().get("var1"), is("Gd"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var2"), is("d"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var3"), is("d"));
    }

    private RunContext performStoryRun(boolean skipScenariosAfterGivenStoriesFailure, String givenStoryAsString) {
        String givenStoryPath = "given/path";
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, new GivenStories(givenStoryPath),
                singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doSkipStoryIfGivenStoryFailed(skipScenariosAfterGivenStoriesFailure));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(givenStoryPath)).thenReturn(givenStoryAsString);
        List<CandidateSteps> candidateSteps = emptyList();
        AllStepCandidates allStepCandidates = new AllStepCandidates(candidateSteps);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates, embedderMonitor,
                new MetaFilter(), failures);
        performableTree.addStories(runContext, singletonList(story));
        performableTree.perform(runContext, story);
        return runContext;
    }

    private Meta isEmptyMeta() {
        return argThat(meta -> meta.getPropertyNames().isEmpty());
    }

    private static class DefaultParameterConverters extends ParameterConverters {
        @Override
        public Object convert(String value, Type type) {
            return value;
        }
    }

    @Test
    void performStoryIfDryRunTrue() {
        Scenario scenario = new Scenario("scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, null,
                singletonList(scenario));
        Configuration configuration = mock(Configuration.class);
        when(configuration.stepCollector()).thenReturn(new MarkUnmatchedStepsAsPending());
        when(configuration.storyControls()).thenReturn(new StoryControls());
        when(configuration.stepsContext()).thenReturn(new StepsContext());
        when(configuration.dryRun()).thenReturn(true);
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        StoryReporter storyReporter = mock(StoryReporter.class);
        when(configuration.storyReporter(STORY_PATH)).thenReturn(storyReporter);
        List<CandidateSteps> candidateSteps = emptyList();
        AllStepCandidates allStepCandidates = new AllStepCandidates(candidateSteps);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates, embedderMonitor,
                new MetaFilter(), failures);
        performableTree.addStories(runContext, singletonList(story));
        performableTree.perform(runContext, story);

        verify(storyReporter).dryRun();
    }

    @Test
    void shouldNotResetFailuresBetweenStories() {
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story1 = new Story("path1", null, null, null, new GivenStories("given/path1"),
                singletonList(scenario));
        Story story2 = new Story("path2", null, null, null, new GivenStories("given/path2"),
                singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doResetStateBeforeScenario(false));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(anyString())).thenReturn(GIVEN_SCENARIO_FAIL);
        List<CandidateSteps> candidateSteps = new InstanceStepsFactory(configuration, new Steps())
                .createCandidateSteps();
        AllStepCandidates allStepCandidates = new AllStepCandidates(candidateSteps);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = new BatchFailures();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates, embedderMonitor,
                new MetaFilter(), failures);
        performableTree.addStories(runContext, Arrays.asList(story1, story2));
        performableTree.perform(runContext, story1);
        performableTree.perform(runContext, story2);

        assertThat(failures.size(), is(2));
    }

    @Test
    void shouldResetFailuresOnReRun() {
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, new GivenStories("given/path"),
                singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doResetStateBeforeScenario(false));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(anyString())).thenReturn(GIVEN_SCENARIO_FAIL);
        List<CandidateSteps> candidateSteps = new InstanceStepsFactory(configuration, new Steps())
                .createCandidateSteps();
        AllStepCandidates allStepCandidates = new AllStepCandidates(candidateSteps);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = new BatchFailures();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates, embedderMonitor,
                new MetaFilter(), failures);
        performableTree.addStories(runContext, singletonList(story));
        performableTree.perform(runContext, story);
        performableTree.perform(runContext, story);

        assertThat(failures.size(), is(1));
    }

    @Test
    void shouldNotShareStoryStateBetweenThreads() throws Throwable {
        RunContext context = runStoryInContext();
        assertThat(context.state().getClass().getSimpleName(), is("FineSoFar"));
        assertReturnsNullInAnotherThread(context::state);
    }

    @Test
    void shouldNotShareStoryPathBetweenThreads() throws Throwable {
        RunContext context = runStoryInContext();
        assertThat(context.path(), is(STORY_PATH));
        assertReturnsNullInAnotherThread(context::path);
    }

    @Test
    void shouldNotShareStoryReporterBetweenThreads() throws Throwable {
        RunContext context = runStoryInContext();
        assertThat(context.reporter(), instanceOf(StoryReporter.class));
        assertReturnsNullInAnotherThread(context::reporter);
    }

    private RunContext runStoryInContext() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useStoryLoader(mock(StoryLoader.class));
        PerformableTree performableTree = new PerformableTree();
        AllStepCandidates allStepCandidates = new AllStepCandidates(emptyList());
        RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates,
                mock(EmbedderMonitor.class), new MetaFilter(), mock(BatchFailures.class));
        Story story = EMPTY_STORY;
        performableTree.addStories(runContext, singletonList(story));
        performableTree.perform(runContext, story);
        return runContext;
    }

    private void assertReturnsNullInAnotherThread(Supplier<Object> supplier) throws Throwable {
        Callable<Void> task = () -> {
            assertThat(supplier.get(), is(nullValue()));
            return null;
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
