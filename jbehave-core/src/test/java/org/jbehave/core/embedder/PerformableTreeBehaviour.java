package org.jbehave.core.embedder;

import org.jbehave.core.annotations.Scope;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.*;
import org.jbehave.core.steps.context.StepsContext;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.steps.StepCollector.Stage;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

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
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(candidateSteps, Stage.BEFORE);
        ordered.verify(stepCollector).collectLifecycleSteps(eq(candidateSteps), eq(story.getLifecycle()),
                any(Meta.class), eq(Scope.STORY), any(MatchingStepMonitor.class));
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(candidateSteps, story, Stage.BEFORE,
                false);
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(candidateSteps, story, Stage.AFTER,
                false);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(candidateSteps, Stage.AFTER);
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

    @Test
    public void shouldReplaceParameters() {
        ParameterControls parameterControls = new ParameterControls();
        PerformableTree performableTree = new PerformableTree();
        Configuration configuration = mock(Configuration.class);
        when(configuration.storyControls()).thenReturn(new StoryControls());
        List<CandidateSteps> candidateSteps = Collections.emptyList();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);
        PerformableTree.RunContext context = spy(performableTree.newRunContext(configuration, candidateSteps,
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
        ExamplesTable scenarioExamplesTable = new ExamplesTable("").withRows(
                Arrays.asList(scenarioExample, scenarioExampleSecond));

        String scenarioTitle = "scenario title";
        Scenario scenario = new Scenario(scenarioTitle, new Meta(), givenStories, scenarioExamplesTable, Collections.<String>emptyList());

        Narrative narrative = mock(Narrative.class);
        Lifecycle lifecycle = mock(Lifecycle.class);
        Story story = new Story(null, null, new Meta(), narrative, givenStories, lifecycle,
                Collections.singletonList(scenario));

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
        lifecycleSteps.put(Stage.BEFORE, Collections.<Step>emptyList());
        lifecycleSteps.put(Stage.AFTER, Collections.<Step>emptyList());
        when(stepCollector.collectLifecycleSteps(eq(candidateSteps), eq(lifecycle), isEmptyMeta(), eq(Scope.STORY),
                any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);
        when(stepCollector.collectLifecycleSteps(eq(candidateSteps), eq(lifecycle), isEmptyMeta(), eq(Scope.SCENARIO),
                any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        performableTree.addStories(context, Collections.singletonList(story));
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

    private Meta isEmptyMeta() {
        return argThat(meta -> meta.getPropertyNames().isEmpty());
    }

    private class DefaultParameterConverters extends ParameterConverters {
        public Object convert(String value, Type type) {
            return value;
        }
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
                assertThat(supplier.get(), is(nullValue()));
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
