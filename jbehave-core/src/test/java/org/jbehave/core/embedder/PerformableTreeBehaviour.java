package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.PerformableTree.ExamplePerformableScenario;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.Timing;
import org.jbehave.core.steps.context.StepsContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class PerformableTreeBehaviour {

    private static final String GIVEN_SCENARIO_FAIL = "Scenario: given scenario title\nWhen I fail";
    private static final String STORY_PATH = "path";
    private static final Story EMPTY_STORY = new Story(STORY_PATH, null, null, null, null, emptyList());
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Test
    void shouldAddExcludedPerformableScenariosToPerformableStory() {
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
        AllStepCandidates allStepCandidates = new AllStepCandidates(configuration.stepConditionMatcher(),
                singletonList(candidate));
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        MetaFilter filter = new MetaFilter("-skip", embedderMonitor);
        BatchFailures failures = mock(BatchFailures.class);

        Scenario scenario = new Scenario("scenario title", Meta.createMeta("@skip", new Keywords()));
        Meta storyMeta = new Meta();
        Story story = new Story(STORY_PATH, null, storyMeta, null, singletonList(scenario));

        Map<Stage, List<Step>> lifecycleSteps = new LinkedHashMap<>();
        lifecycleSteps.put(Stage.BEFORE, emptyList());
        lifecycleSteps.put(Stage.AFTER, emptyList());
        when(stepCollector.collectLifecycleSteps(eq(stepCandidates), eq(story.getLifecycle()), eq(storyMeta),
                eq(Scope.STORY), eq(new HashMap<>()), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates,
                embedderMonitor, filter, failures);
        performableTree.addStories(runContext, singletonList(story));

        assertThat(performableTree.getRoot().getStories().get(0).getScenarios().size(), equalTo(1));

        InOrder ordered = inOrder(stepCollector);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(beforeStories);
        ordered.verify(stepCollector).collectLifecycleSteps(eq(stepCandidates), eq(story.getLifecycle()),
                eq(storyMeta), eq(Scope.STORY), eq(new HashMap<>()), any(MatchingStepMonitor.class));
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(beforeStory, storyMeta);
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(afterStory, storyMeta);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(afterStories);
        verifyNoMoreInteractions(stepCollector);
    }

    @Test
    void shouldNotSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(new StoryControls().doSkipStoryIfGivenStoryFailed(false),
                GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(false));
    }

    @Test
    void shouldSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(new StoryControls().doSkipStoryIfGivenStoryFailed(true),
                GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(true));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    @Test
    void shouldNotSkipStoryWhenGivenStoryIsPassed() {
        RunContext context = performStoryRun(new StoryControls().doSkipStoryIfGivenStoryFailed(true),
                "Scenario: given scenario title");
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    @Test
    void shouldResetCurrentStoryControlsOnlyForRootStory() {
        StoryControls storyControls = mock(StoryControls.class);
        performStoryRun(storyControls, GIVEN_SCENARIO_FAIL);
        verify(storyControls).resetCurrentStoryControls();
    }

    @Test
    void shouldResetCurrentStoryControls() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.storyControls().currentStoryControls().doResetStateBeforeStory(false);
        configuration.storyControls().currentStoryControls().doResetStateBeforeScenario(false);
        configuration.storyControls().currentStoryControls().doSkipStoryIfGivenStoryFailed(true);
        configuration.storyControls().currentStoryControls().doIgnoreMetaFiltersIfGivenStory(true);
        configuration.useStoryLoader(mock(StoryLoader.class));
        PerformableTree performableTree = new PerformableTree();
        Story story = EMPTY_STORY;
        RunContext runContext = createRunContext(configuration, performableTree, mock(BatchFailures.class),
                singletonList(story));
        performableTree.perform(runContext, story);
        StoryControls storyControls = configuration.storyControls();
        assertThat(storyControls.resetStateBeforeStory(), is(true));
        assertThat(storyControls.resetStateBeforeScenario(), is(true));
        assertThat(storyControls.skipStoryIfGivenStoryFailed(), is(false));
        assertThat(storyControls.skipScenariosAfterFailure(), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReplaceParameters() {
        ParameterControls parameterControls = new ParameterControls();
        Configuration configuration = mock(Configuration.class);
        when(configuration.storyControls()).thenReturn(new StoryControls());

        StoryControls storyControls = mock(StoryControls.class);
        when(configuration.storyControls()).thenReturn(storyControls);
        when(storyControls.skipBeforeAndAfterScenarioStepsIfGivenStory()).thenReturn(false);
        when(configuration.parameterConverters()).thenReturn(new DefaultParameterConverters());
        when(configuration.parameterControls()).thenReturn(parameterControls);

        Keywords keywords = mock(Keywords.class);
        when(configuration.keywords()).thenReturn(keywords);

        StepMonitor stepMonitor = mock(StepMonitor.class);
        when(configuration.stepMonitor()).thenReturn(stepMonitor);
        StepCollector stepCollector = mock(StepCollector.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);

        GivenStories givenStories = new GivenStories("");

        Map<String, String> scenarioExamplesRow1 = createExamplesRow("var1", "E", "var3", "<var2>F");
        ExamplesTable scenarioExamplesTable = ExamplesTable.empty().withRows(asList(
                scenarioExamplesRow1,
                createExamplesRow("var1", "G<var2>", "var3", "<var2>")
        ));

        String scenarioTitle = "scenario title";
        Scenario scenario = new Scenario(scenarioTitle, new Meta(), givenStories, scenarioExamplesTable, emptyList());

        Lifecycle lifecycle = mock(Lifecycle.class);
        Story story = new Story(null, null, new Meta(), mock(Narrative.class), givenStories, lifecycle,
                singletonList(scenario));

        ExamplesTable storyExamplesTable = ExamplesTable.empty().withRows(asList(
                createExamplesRow("var1", "a", "var2", "b"),
                createExamplesRow("var1", "c", "var2", "d")
        ));
        when(lifecycle.getExamplesTable()).thenReturn(storyExamplesTable);

        Map<Stage, List<Step>> lifecycleSteps = new EnumMap<>(Stage.class);
        lifecycleSteps.put(Stage.BEFORE, emptyList());
        lifecycleSteps.put(Stage.AFTER, emptyList());

        ArgumentCaptor<Map<String, String>> storyParametersCaptor = ArgumentCaptor.forClass(Map.class);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.STORY),
                storyParametersCaptor.capture(), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        ArgumentCaptor<Map<String, String>> scenarioParametersCaptor = ArgumentCaptor.forClass(Map.class);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.SCENARIO),
                scenarioParametersCaptor.capture(), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        PerformableTree performableTree = new PerformableTree();
        createRunContext(configuration, performableTree, mock(BatchFailures.class), singletonList(story));
        List<PerformableTree.PerformableScenario> performableScenarios = performableTree.getRoot().getStories().get(0)
                .getScenarios();

        assertThat(performableScenarios.size(), is(scenarioExamplesRow1.size()));
        assertThat(performableScenarios.get(0).getScenario().getTitle(), is(scenarioTitle + " [1]"));
        List<PerformableTree.ExamplePerformableScenario> examplePerformableScenarios = performableScenarios.get(0)
                .getExamples();
        assertThat(examplePerformableScenarios.size(), is(scenarioExamplesRow1.size()));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var1"), is("E"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var2"), is("b"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var3"), is("bF"));

        assertThat(examplePerformableScenarios.get(1).getParameters().get("var1"), is("Gb"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var2"), is("b"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var3"), is("b"));

        assertThat(performableScenarios.get(1).getScenario().getTitle(), is(scenarioTitle + " [2]"));
        examplePerformableScenarios = performableScenarios.get(1).getExamples();
        assertThat(examplePerformableScenarios.size(), is(scenarioExamplesRow1.size()));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var1"), is("E"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var2"), is("d"));
        assertThat(examplePerformableScenarios.get(0).getParameters().get("var3"), is("dF"));

        assertThat(examplePerformableScenarios.get(1).getParameters().get("var1"), is("Gd"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var2"), is("d"));
        assertThat(examplePerformableScenarios.get(1).getParameters().get("var3"), is("d"));

        List<Map<String, String>> storyParameters = new ArrayList<>();
        storyParameters.add(new HashMap<>());
        assertEquals(storyParameters, storyParametersCaptor.getAllValues());

        List<Map<String, String>> scenarioParameters = new ArrayList<>();
        scenarioParameters.add(performableScenarios.get(0).getExamples().get(0).getParameters());
        scenarioParameters.add(performableScenarios.get(0).getExamples().get(1).getParameters());
        scenarioParameters.add(performableScenarios.get(1).getExamples().get(0).getParameters());
        scenarioParameters.add(performableScenarios.get(1).getExamples().get(1).getParameters());
        assertEquals(scenarioParameters, scenarioParametersCaptor.getAllValues());
    }
    
    @Test
    void shouldRemoveMetaFromFinalScenarioParameters() {
        ParameterControls parameterControls = new ParameterControls();
        Configuration configuration = mock(Configuration.class);
        when(configuration.storyControls()).thenReturn(new StoryControls());

        StoryControls storyControls = mock(StoryControls.class);
        when(configuration.storyControls()).thenReturn(storyControls);
        when(storyControls.skipBeforeAndAfterScenarioStepsIfGivenStory()).thenReturn(false);
        when(configuration.parameterConverters()).thenReturn(new DefaultParameterConverters());
        when(configuration.parameterControls()).thenReturn(parameterControls);

        when(configuration.keywords()).thenReturn(new Keywords());

        StepMonitor stepMonitor = mock(StepMonitor.class);
        when(configuration.stepMonitor()).thenReturn(stepMonitor);
        StepCollector stepCollector = mock(StepCollector.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);

        ExamplesTable scenarioExamplesTable = ExamplesTable.empty().withRows(asList(
                createExamplesRow("Meta:", "@test", "value", "1")
        ));

        Scenario scenario = new Scenario("", new Meta(), GivenStories.EMPTY, scenarioExamplesTable,
                emptyList());

        Lifecycle lifecycle = mock(Lifecycle.class);
        Story story = new Story(null, null, new Meta(), mock(Narrative.class), GivenStories.EMPTY, lifecycle,
                singletonList(scenario));

        when(lifecycle.getExamplesTable()).thenReturn(ExamplesTable.EMPTY);

        Map<Stage, List<Step>> lifecycleSteps = new EnumMap<>(Stage.class);
        lifecycleSteps.put(Stage.BEFORE, emptyList());
        lifecycleSteps.put(Stage.AFTER, emptyList());

        ArgumentCaptor<Map<String, String>> storyParametersCaptor = ArgumentCaptor.forClass(Map.class);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.STORY),
                storyParametersCaptor.capture(), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        ArgumentCaptor<Map<String, String>> scenarioParametersCaptor = ArgumentCaptor.forClass(Map.class);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.SCENARIO),
                scenarioParametersCaptor.capture(), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        PerformableTree performableTree = new PerformableTree();
        createRunContext(configuration, performableTree, mock(BatchFailures.class), singletonList(story));
        List<PerformableTree.PerformableScenario> performableScenarios = performableTree.getRoot().getStories().get(0)
                .getScenarios();

        assertThat(performableScenarios.size(), is(1));
        List<ExamplePerformableScenario> exampleScenarios = performableScenarios.get(0).getExamples();
        assertThat(exampleScenarios.size(), is(1));
        assertThat(singletonMap("value", "1"), equalTo(exampleScenarios.get(0).getParameters()));

    }

    private Map<String, String> createExamplesRow(String key1, String value1, String key2, String value2) {
        Map<String, String> storyExampleFirstRow = new HashMap<>();
        storyExampleFirstRow.put(key1, value1);
        storyExampleFirstRow.put(key2, value2);
        return storyExampleFirstRow;
    }

    private RunContext performStoryRun(StoryControls storyControls, String givenStoryAsString) {
        String givenStoryPath = "given/path";
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, new GivenStories(givenStoryPath),
                singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration().useStoryControls(storyControls);
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(givenStoryPath)).thenReturn(givenStoryAsString);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = createRunContext(configuration, performableTree, mock(BatchFailures.class),
                singletonList(story));
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

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = createRunContext(configuration, performableTree, mock(BatchFailures.class),
                singletonList(story));
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
        BatchFailures failures = new BatchFailures();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = createRunContext(configuration, performableTree, failures, asList(story1, story2),
                new Steps());
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
        BatchFailures failures = new BatchFailures();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = createRunContext(configuration, performableTree, failures, singletonList(story),
                new Steps());
        performableTree.perform(runContext, story);
        performableTree.perform(runContext, story);

        assertThat(failures.size(), is(1));
    }

    @Test
    void shouldReportIgnorableSteps() {
        String step1 = "When I ignore";
        testIgnorableSteps(step1, (ordered, storyReporter) -> {
            ordered.verify(storyReporter).beforeStep(argThat(step -> step1.equals(step.getStepAsString())
                    && StepExecutionType.EXECUTABLE == step.getExecutionType()));
            ordered.verify(storyReporter).ignorable(step1);
        });
    }

    @Test
    void shouldReportIgnorableStepsFromCompositeStep() {
        String step1 = "When I ignore from composed step";
        testIgnorableSteps(step1, (ordered, storyReporter) -> {
            ordered.verify(storyReporter).beforeStep(argThat(step -> step1.equals(step.getStepAsString())
                    && StepExecutionType.EXECUTABLE == step.getExecutionType()));
            ordered.verify(storyReporter).beforeComposedSteps();
            ordered.verify(storyReporter).beforeStep(argThat(step -> "When I ignore".equals(step.getStepAsString())
                    && StepExecutionType.EXECUTABLE == step.getExecutionType()));
            ordered.verify(storyReporter).ignorable("When I ignore");
            ordered.verify(storyReporter).afterComposedSteps();
            ordered.verify(storyReporter).ignorable(step1);
        });
    }

    private void testIgnorableSteps(String step1, BiConsumer<InOrder, StoryReporter> ignorableStepVerifier) {
        String step2 = "When I fail";
        Scenario scenario = new Scenario("scenario with ignorable", Meta.EMPTY, null, null, asList(step1, step2));
        Narrative narrative = mock(Narrative.class);
        Lifecycle lifecycle = new Lifecycle();
        Story story = new Story(STORY_PATH, null, null, narrative, null, lifecycle, singletonList(scenario));

        StoryReporter storyReporter = mock(StoryReporter.class);
        StoryReporterBuilder storyReporterBuilder = mock(StoryReporterBuilder.class);
        when(storyReporterBuilder.build(STORY_PATH)).thenReturn(storyReporter);
        Configuration configuration = new MostUsefulConfiguration().useStoryReporterBuilder(storyReporterBuilder);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = createRunContext(configuration, performableTree, mock(BatchFailures.class),
                singletonList(story), new Steps());
        performableTree.perform(runContext, story);
        InOrder ordered = inOrder(storyReporter);
        ordered.verify(storyReporter).beforeStory(story, false);
        ordered.verify(storyReporter).narrative(narrative);
        ordered.verify(storyReporter).lifecycle(lifecycle);
        verifyBeforeStoryStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).beforeScenario(scenario);
        verifyBeforeScenarioStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).beforeScenarioSteps(null, null);
        ignorableStepVerifier.accept(ordered, storyReporter);
        ordered.verify(storyReporter).beforeStep(argThat(step -> step2.equals(step.getStepAsString())
                && StepExecutionType.IGNORABLE == step.getExecutionType()));
        ordered.verify(storyReporter).ignorable(step2);
        ordered.verify(storyReporter).afterScenarioSteps(null, null);
        verifyAfterScenarioStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).afterScenario(any(Timing.class));
        verifyAfterStoryStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).afterStory(false);
        verifyNoMoreInteractions(storyReporter);
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

    @Test
    void shouldPerformAfterHooksUponStoryExecutionTimeout() {
        String step1 = "When I execute step 1";
        String step2 = "When I execute step 2";
        String step3 = "When I execute step 3";
        Scenario scenario = new Scenario("Scenario to be timed out", Meta.EMPTY, null, null,
                asList(step1, step2, step3));
        Narrative narrative = mock(Narrative.class);
        Lifecycle lifecycle = new Lifecycle();
        Story story = new Story(STORY_PATH, null, null, narrative, null, lifecycle, singletonList(scenario));

        StoryReporter storyReporter = mock(StoryReporter.class);
        StoryReporterBuilder storyReporterBuilder = mock(StoryReporterBuilder.class);
        when(storyReporterBuilder.build(STORY_PATH)).thenReturn(storyReporter);
        Configuration configuration = new MostUsefulConfiguration().useStoryReporterBuilder(storyReporterBuilder);

        Steps steps = new Steps();

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = createRunContext(configuration, performableTree, mock(BatchFailures.class),
                singletonList(story), steps);

        steps.runContext = runContext;
        steps.story = story;

        UUIDExceptionWrapper uuidExceptionWrapper = assertThrows(UUIDExceptionWrapper.class,
                () -> performableTree.perform(runContext, story));
        Throwable cause = uuidExceptionWrapper.getCause();
        assertEquals(InterruptedException.class, cause.getClass());
        assertEquals(STORY_PATH, cause.getMessage());

        assertTrue(steps.step1Invoked);
        assertTrue(steps.step2Invoked);
        assertFalse(steps.step3Invoked);

        InOrder ordered = inOrder(storyReporter);
        ordered.verify(storyReporter).beforeStory(story, false);
        ordered.verify(storyReporter).narrative(narrative);
        ordered.verify(storyReporter).lifecycle(lifecycle);
        verifyBeforeStoryStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).beforeScenario(scenario);
        verifyBeforeScenarioStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).beforeScenarioSteps(null, null);

        ordered.verify(storyReporter).beforeStep(argThat(step -> step1.equals(step.getStepAsString())
                && StepExecutionType.EXECUTABLE == step.getExecutionType()));
        ordered.verify(storyReporter).successful(step1);
        ordered.verify(storyReporter).beforeStep(argThat(step -> step2.equals(step.getStepAsString())
                && StepExecutionType.EXECUTABLE == step.getExecutionType()));
        ordered.verify(storyReporter).successful(step2);
        ordered.verify(storyReporter).afterScenarioSteps(null, null);
        verifyAfterScenarioStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).afterScenario(any(Timing.class));
        verifyAfterStoryStepsHooks(ordered, storyReporter);
        ordered.verify(storyReporter).storyCancelled(story, steps.storyDuration);
        ordered.verify(storyReporter).afterStory(false);
        verifyNoMoreInteractions(storyReporter);
    }

    private RunContext runStoryInContext() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useStoryLoader(mock(StoryLoader.class));
        PerformableTree performableTree = new PerformableTree();
        Story story = EMPTY_STORY;
        RunContext runContext = createRunContext(configuration, performableTree, mock(BatchFailures.class),
                singletonList(story));
        performableTree.perform(runContext, story);
        return runContext;
    }

    private static RunContext createRunContext(Configuration configuration, PerformableTree performableTree,
            BatchFailures batchFailures, List<Story> stories, Object... steps) {
        InstanceStepsFactory stepsFactory = new InstanceStepsFactory(configuration, steps);
        AllStepCandidates allStepCandidates = new AllStepCandidates(configuration.stepConditionMatcher(),
                stepsFactory.createCandidateSteps());
        RunContext runContext = performableTree.newRunContext(configuration, allStepCandidates,
                mock(EmbedderMonitor.class), new MetaFilter(), batchFailures);
        performableTree.addStories(runContext, stories);
        return runContext;
    }

    private static void verifyBeforeStoryStepsHooks(InOrder ordered, StoryReporter storyReporter) {
        ordered.verify(storyReporter).beforeStorySteps(Stage.BEFORE, ExecutionType.SYSTEM);
        ordered.verify(storyReporter).afterStorySteps(Stage.BEFORE, ExecutionType.SYSTEM);
        ordered.verify(storyReporter).beforeStorySteps(Stage.BEFORE, ExecutionType.USER);
        ordered.verify(storyReporter).afterStorySteps(Stage.BEFORE, ExecutionType.USER);
        ordered.verify(storyReporter).beforeScenarios();
    }

    private static void verifyBeforeScenarioStepsHooks(InOrder ordered, StoryReporter storyReporter) {
        ordered.verify(storyReporter).beforeScenarioSteps(Stage.BEFORE, ExecutionType.SYSTEM);
        ordered.verify(storyReporter).afterScenarioSteps(Stage.BEFORE, ExecutionType.SYSTEM);
        ordered.verify(storyReporter).beforeScenarioSteps(Stage.BEFORE, ExecutionType.USER);
        ordered.verify(storyReporter).afterScenarioSteps(Stage.BEFORE, ExecutionType.USER);
    }

    private static void verifyAfterScenarioStepsHooks(InOrder ordered, StoryReporter storyReporter) {
        ordered.verify(storyReporter).beforeScenarioSteps(Stage.AFTER, ExecutionType.USER);
        ordered.verify(storyReporter).afterScenarioSteps(Stage.AFTER, ExecutionType.USER);
        ordered.verify(storyReporter).beforeScenarioSteps(Stage.AFTER, ExecutionType.SYSTEM);
        ordered.verify(storyReporter).afterScenarioSteps(Stage.AFTER, ExecutionType.SYSTEM);
    }

    private static void verifyAfterStoryStepsHooks(InOrder ordered, StoryReporter storyReporter) {
        ordered.verify(storyReporter).afterScenarios();
        ordered.verify(storyReporter).beforeStorySteps(Stage.AFTER, ExecutionType.USER);
        ordered.verify(storyReporter).afterStorySteps(Stage.AFTER, ExecutionType.USER);
        ordered.verify(storyReporter).beforeStorySteps(Stage.AFTER, ExecutionType.SYSTEM);
        ordered.verify(storyReporter).afterStorySteps(Stage.AFTER, ExecutionType.SYSTEM);
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

        private boolean step1Invoked;
        private boolean step2Invoked;
        private boolean step3Invoked;

        private RunContext runContext;
        private Story story;
        private StoryDuration storyDuration;

        @When("I fail")
        public void fail() {
            throw new RuntimeException();
        }

        @When("I ignore")
        public void ignore() {
            throw new IgnoringStepsFailure("next steps in the scenario should be ignored");
        }

        @When("I ignore from composed step")
        @Composite(steps = { "When I ignore" })
        public void ignoreFromComposite() {
        }

        @When("I execute step 1")
        public void executeStep1() {
            this.step1Invoked = true;
        }

        @When("I execute step 2")
        public void executeStep2() {
            this.step2Invoked = true;

            // Emulating story execution timeout
            this.storyDuration = mock(StoryDuration.class);
            this.runContext.cancelStory(this.story, storyDuration);
        }

        @When("I execute step 3")
        public void executeStep3() {
            this.step3Invoked = true;
        }
    }
}
