package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.MatchingStepMonitor.StepMatch;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.jbehave.core.failures.PendingStepsFound;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.RestartingStoryFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractStepResult;
import org.jbehave.core.steps.PendingStepMethodGenerator;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.StepResult;
import org.jbehave.core.steps.Timer;
import org.jbehave.core.steps.Timing;
import org.jbehave.core.steps.context.StepsContext;

/**
 * Creates a tree of {@link Performable} objects for a set of stories, grouping
 * sets of performable steps for each story and scenario, and adding before and
 * after stories steps. The process has two phases:
 * <ol>
 * <li>The tree is populated with groups of performable steps when the stories
 * are added via the {@link #addStories(RunContext, List)} method.</li>
 * <li>The performable steps are then populated with the results when the
 * {@link #performBeforeOrAfterStories(RunContext, Stage)} and
 * {@link #perform(RunContext, Story)} methods are executed.</li>
 * </ol>
 * The tree is created per {@link RunContext} for the set of stories being run
 * but the individual stories can be performed concurrently.
 */
public class PerformableTree {

    private static final Map<String, String> NO_PARAMETERS = new HashMap<>();

    private PerformableRoot root = new PerformableRoot();

    public PerformableRoot getRoot() {
        return root;
    }

    public void addStories(RunContext context, List<Story> stories) {
        root.addBeforeSteps(context.beforeStoriesSteps());
        for (Story story : stories) {
            root.add(performableStory(context, story, NO_PARAMETERS));
        }
        root.addAfterSteps(context.afterStoriesSteps());
    }

    private PerformableStory performableStory(RunContext context, Story story, Map<String, String> storyParameters) {
        PerformableStory performableStory = new PerformableStory(story, context.configuration().keywords(),
                context.givenStory());

        FilteredStory filteredStory = context.filter(story);
        Meta storyMeta = story.getMeta();
        boolean storyExcluded = filteredStory.excluded();

        performableStory.excluded(storyExcluded);

        if (!storyExcluded) {

            Map<Stage, PerformableSteps> lifecycleSteps = context.lifecycleSteps(story.getLifecycle(), storyMeta,
                    Scope.STORY);

            performableStory.addBeforeSteps(ExecutionType.SYSTEM, context.beforeStorySteps(storyMeta));
            performableStory.addBeforeSteps(ExecutionType.USER, lifecycleSteps.get(Stage.BEFORE));
            performableStory.addAll(performableScenarios(context, story, storyParameters, filteredStory));

            // Add Given stories only if story contains non-filtered scenarios
            if (performableStory.hasIncludedScenarios()) {
                Map<String, String> givenStoryParameters = new HashMap<>(storyParameters);
                addMetaParameters(givenStoryParameters, storyMeta);
                performableStory.setGivenStories(performableGivenStories(context, story.getGivenStories(),
                        givenStoryParameters));
            }

            performableStory.addAfterSteps(ExecutionType.USER, lifecycleSteps.get(Stage.AFTER));
            performableStory.addAfterSteps(ExecutionType.SYSTEM, context.afterStorySteps(storyMeta));

        }

        return performableStory;
    }

    private List<PerformableScenario> performableScenarios(RunContext context, Story story,
            Map<String, String> storyParameters, FilteredStory filterContext) {
        List<PerformableScenario> performableScenarios = new ArrayList<PerformableScenario>();

        ExamplesTable storyExamplesTable = story.getLifecycle().getExamplesTable();
        List<Map<String, String>> storyExamplesTableRows;
        if (storyExamplesTable.isEmpty()) {
            storyExamplesTableRows = new ArrayList<Map<String, String>>();
            storyExamplesTableRows.add(new HashMap<String, String>());
        } else {
            storyExamplesTableRows = storyExamplesTable.getRows();
        }

        // determine if before and after scenario steps should be run
        boolean runBeforeAndAfterScenarioSteps = shouldRunBeforeOrAfterScenarioSteps(context);
        for (Map<String, String> storyExamplesTableRow : storyExamplesTableRows) {
            for (Map.Entry<String, String> entry : storyExamplesTableRow.entrySet()) {
                entry.setValue((String)
                        context.configuration().parameterConverters().convert(entry.getValue(), String.class));
            }
        }
        for (int i = 0; i < storyExamplesTableRows.size(); i++) {
            Map<String, String> storyExamplesTableRow = storyExamplesTableRows.get(i);
            for (Scenario scenario : story.getScenarios()) {
                Map<String, String> scenarioParameters = new HashMap<String, String>(storyParameters);
                PerformableScenario performableScenario = performableScenario(context, story, scenarioParameters,
                        filterContext, runBeforeAndAfterScenarioSteps, scenario, storyExamplesTableRow,
                        storyExamplesTable.isEmpty() ? -1 : i);
                if (performableScenario.isPerformable()) {
                    performableScenarios.add(performableScenario);
                }
            }
        }
        return performableScenarios;
    }

    private PerformableScenario performableScenario(RunContext context, Story story,
            Map<String, String> storyParameters, FilteredStory filterContext, boolean runBeforeAndAfterScenarioSteps,
            Scenario originalScenario, Map<String, String> storyExamplesTableRow, int storyExamplesTableRowIndex) {
        Scenario scenario = originalScenario;
        if (storyExamplesTableRowIndex != -1) {
            scenario = new Scenario(scenario.getTitle() + " [" + (storyExamplesTableRowIndex + 1) + "]",
                scenario.getMeta(),  scenario.getGivenStories(), scenario.getExamplesTable(), scenario.getSteps());
        }
        PerformableScenario performableScenario = new PerformableScenario(scenario, story.getPath());
        if (context.failureOccurred() && context.configuration().storyControls().skipScenariosAfterFailure()) {
            return performableScenario;
        }

        boolean scenarioExcluded = filterContext.excluded(originalScenario);

        performableScenario.excluded(scenarioExcluded);

        if (!scenarioExcluded) {
            Meta storyAndScenarioMeta = scenario.getMeta().inheritFrom(story.getMeta());

            if (isParameterisedByExamples(scenario)) {
                ExamplesTable table = scenario.getExamplesTable();
                List<Map<String, String>> tableRows = table.getRows();
                for (int exampleIndex = 0; exampleIndex < tableRows.size(); exampleIndex++) {
                    Map<String, String> scenarioParameters = tableRows.get(exampleIndex);
                    Map<String, String> scenarioParametersCopy = new HashMap<String, String>(storyParameters);
                    scenarioParametersCopy.putAll(storyExamplesTableRow);
                    scenarioParametersCopy.putAll(scenarioParameters);
                    for (Map.Entry<String, String> scenarioParameterEntry : scenarioParametersCopy.entrySet()) {
                        String value = context.configuration().parameterControls().replaceAllDelimitedNames(
                                scenarioParameterEntry.getValue(), storyExamplesTableRow);
                        scenarioParameterEntry.setValue((String) context.configuration().parameterConverters()
                                 .convert(value, String.class));
                    }
                    Map<String, String> parameters = new LinkedHashMap<String, String>(scenarioParametersCopy);
                    for (Map.Entry<String, String> storyExamplesTableRowEntry: storyExamplesTableRow.entrySet()) {
                        String key = storyExamplesTableRowEntry.getKey();
                        if (!parameters.containsKey(key)) {
                            parameters.put(key, storyExamplesTableRowEntry.getValue());
                        }
                    }
                    addExampleScenario(context, scenario, performableScenario, story, storyAndScenarioMeta,
                            parameters, exampleIndex);
                }
            } else if (!storyExamplesTableRow.isEmpty()) {
                addExampleScenario(context, scenario, performableScenario, story, storyAndScenarioMeta,
                        new HashMap<>(storyExamplesTableRow), -1);
            } else { // plain old scenario
                NormalPerformableScenario normalScenario = normalScenario(context, story, scenario,
                        storyAndScenarioMeta, storyParameters);

                // run before scenario steps, if allowed
                if (runBeforeAndAfterScenarioSteps) {
                    normalScenario.addBeforeSteps(ExecutionType.SYSTEM,
                            context.beforeScenarioSteps(storyAndScenarioMeta, ScenarioType.NORMAL));
                }
                performableScenario.useNormalScenario(normalScenario);
                // after scenario steps, if allowed
                if (runBeforeAndAfterScenarioSteps) {
                    normalScenario.addAfterSteps(ExecutionType.SYSTEM,
                            context.afterScenarioSteps(storyAndScenarioMeta, ScenarioType.NORMAL));
                }
            }
        }
        return performableScenario;
    }

    private void addExampleScenario(RunContext context, Scenario scenario, PerformableScenario performableScenario,
            Story story, Meta storyAndScenarioMeta, Map<String, String> parameters, int exampleIndex) {
        Meta exampleScenarioMeta = parameterMeta(context, parameters).inheritFrom(storyAndScenarioMeta);

        if (!context.filter().excluded(exampleScenarioMeta)) {
            ExamplePerformableScenario exampleScenario = exampleScenario(context, story, scenario,
                    storyAndScenarioMeta, parameters, exampleIndex);
            performableScenario.addExampleScenario(exampleScenario);
        }
    }

    private NormalPerformableScenario normalScenario(RunContext context, Story story, Scenario scenario,
            Meta storyAndScenarioMeta, Map<String, String> storyParameters) {
        NormalPerformableScenario normalScenario = new NormalPerformableScenario(story, scenario);
        normalScenario.setStoryAndScenarioMeta(storyAndScenarioMeta);
        addStepsWithLifecycle(normalScenario, context, story.getLifecycle(), storyParameters,
                scenario, storyAndScenarioMeta);
        return normalScenario;
    }

    private ExamplePerformableScenario exampleScenario(RunContext context, Story story, Scenario scenario,
            Meta storyAndScenarioMeta, Map<String, String> parameters, int exampleIndex) {
        ExamplePerformableScenario exampleScenario = new ExamplePerformableScenario(story, scenario, parameters,
                exampleIndex);
        exampleScenario.setStoryAndScenarioMeta(storyAndScenarioMeta);
        exampleScenario.addBeforeSteps(ExecutionType.SYSTEM,
                context.beforeScenarioSteps(storyAndScenarioMeta, ScenarioType.EXAMPLE));
        addStepsWithLifecycle(exampleScenario, context, story.getLifecycle(), parameters, scenario,
                storyAndScenarioMeta);
        exampleScenario.addAfterSteps(ExecutionType.SYSTEM,
                context.afterScenarioSteps(storyAndScenarioMeta, ScenarioType.EXAMPLE));
        return exampleScenario;
    }

    private Meta parameterMeta(RunContext context, Map<String, String> parameters) {
        Meta meta = Meta.EMPTY;
        Keywords keywords = context.configuration().keywords();
        String metaText = keywords.meta();
        if (parameters.containsKey(metaText)) {
            meta = Meta.createMeta(parameters.get(metaText), keywords);
        }
        return meta;
    }

    private void addStepsWithLifecycle(AbstractPerformableScenario performableScenario, RunContext context,
            Lifecycle lifecycle, Map<String, String> parameters, Scenario scenario, Meta storyAndScenarioMeta) {
        Map<Stage, PerformableSteps> lifecycleSteps = context.lifecycleSteps(lifecycle, storyAndScenarioMeta,
                Scope.SCENARIO);

        performableScenario.addBeforeSteps(ExecutionType.SYSTEM,
                context.beforeScenarioSteps(storyAndScenarioMeta, ScenarioType.ANY));
        performableScenario.addBeforeSteps(ExecutionType.USER, lifecycleSteps.get(Stage.BEFORE));
        addMetaParameters(parameters, storyAndScenarioMeta);
        performableScenario.setGivenStories(performableGivenStories(context, scenario.getGivenStories(), parameters));
        performableScenario.addSteps(context.scenarioSteps(lifecycle, storyAndScenarioMeta, scenario, parameters));
        performableScenario.addAfterSteps(ExecutionType.USER, lifecycleSteps.get(Stage.AFTER));
        performableScenario.addAfterSteps(ExecutionType.SYSTEM,
                context.afterScenarioSteps(storyAndScenarioMeta, ScenarioType.ANY));
    }

    private PerformableGivenStories performableGivenStories(RunContext context, GivenStories givenStories,
            Map<String, String> parameters) {
        List<PerformableStory> stories = new ArrayList<>();
        if (givenStories.getPaths().size() > 0) {
            for (GivenStory givenStory : givenStories.getStories()) {
                RunContext childContext = context.childContextFor(givenStory);
                // run given story, using any parameters provided
                Story story = storyOfPath(context.configuration(), childContext.path());                
                if (givenStory.hasAnchorParameters()) {
                    story = storyWithMatchingScenarios(story, givenStory.getAnchorParameters());
                }
                parameters.putAll(givenStory.getParameters());
                stories.add(performableStory(childContext, story, parameters));
            }
        }
        return new PerformableGivenStories(stories, givenStories);
    }

    private Story storyWithMatchingScenarios(Story story, Map<String,String> parameters) {
        if (parameters.isEmpty()) {
            return story;
        }
        List<Scenario> scenarios = new ArrayList<>();
        for (Scenario scenario : story.getScenarios()) {
            if (matchesParameters(scenario, parameters)) {
                scenarios.add(scenario);
            }
        }
        return story.cloneWithScenarios(scenarios);
    }

    private boolean matchesParameters(Scenario scenario, Map<String, String> parameters) {
        Meta meta = scenario.getMeta();
        for (String name : parameters.keySet()) {
            if (meta.hasProperty(name)) {
                return meta.getProperty(name).equals(parameters.get(name));
            }
        }
        return false;
    }

    /**
     * Returns the parsed story from the given path
     * 
     * @param configuration the Configuration used to run story
     * @param storyPath the story path
     * @return The parsed Story
     */
    public Story storyOfPath(Configuration configuration, String storyPath) {
        String storyAsText = configuration.storyLoader().loadStoryAsText(storyPath);
        return configuration.storyParser().parseStory(storyAsText, storyPath);
    }

    /**
     * Returns the parsed story from the given text
     * 
     * @param configuration the Configuration used to run story
     * @param storyAsText the story text
     * @param storyId the story Id, which will be returned as story path
     * @return The parsed Story
     */
    public Story storyOfText(Configuration configuration, String storyAsText, String storyId) {
        return configuration.storyParser().parseStory(storyAsText, storyId);
    }

    private void addMetaParameters(Map<String, String> storyParameters, Meta meta) {
        for (String name : meta.getPropertyNames()) {
            if (!storyParameters.containsKey(name)) {
                storyParameters.put(name, meta.getProperty(name));
            }
        }
    }

    private boolean shouldRunBeforeOrAfterScenarioSteps(RunContext context) {
        return !context.configuration().storyControls().skipBeforeAndAfterScenarioStepsIfGivenStory()
                || !context.givenStory();
    }

    private boolean isParameterisedByExamples(Scenario scenario) {
        return !scenario.getExamplesTable().isEmpty() && !scenario.getGivenStories().requireParameters();
    }

    static void generatePendingStepMethods(RunContext context, List<Step> steps) {
        List<PendingStep> pendingSteps = new ArrayList<>();
        for (Step step : steps) {
            if (step instanceof PendingStep) {
                pendingSteps.add((PendingStep) step);
            }
        }
        if (!pendingSteps.isEmpty()) {
            PendingStepMethodGenerator generator = new PendingStepMethodGenerator(context.configuration().keywords());
            List<String> methods = new ArrayList<>();
            for (PendingStep pendingStep : pendingSteps) {
                if (!pendingStep.annotated()) {
                    methods.add(generator.generateMethod(pendingStep));
                }
            }
        }
    }

    public interface State {

        State run(Step step, List<StepResult> results, Keywords keywords, StoryReporter reporter);

        RuntimeException getFailure();
    }

    private static final class FineSoFar implements State {

        @Override
        public State run(Step step, List<StepResult> results, Keywords keywords, StoryReporter reporter) {
            State state;
            StepResult result;
            int indexOfResult;
            try {
                result = step.perform(reporter, getFailure());

                UUIDExceptionWrapper stepFailure = result.getFailure();
                state = stepFailure == null ? this : new SomethingHappened(stepFailure);
            } catch (IgnoringStepsFailure e) {
                result = AbstractStepResult.ignorable(step.asString(keywords));
                state = new Ignoring(e);
            }
            indexOfResult = results.size();
            results.add(result);

            List<Step> composedSteps = step.getComposedSteps();
            if (!composedSteps.isEmpty()) {
                reporter.beforeComposedSteps();
                for (Step composedStep : composedSteps) {
                    state = state.run(composedStep, results, keywords, reporter);
                }
                reporter.afterComposedSteps();
            }

            if (state instanceof Ignoring) {
                result = AbstractStepResult.ignorable(step.asString(keywords));
                results.set(indexOfResult, result);
            }
            result.describeTo(reporter);
            return state;
        }

        @Override
        public UUIDExceptionWrapper getFailure() {
            return null;
        }

    }

    private static final class SomethingHappened implements State {
        private UUIDExceptionWrapper failure;

        public SomethingHappened(UUIDExceptionWrapper failure) {
            this.failure = failure;
        }

        @Override
        public State run(Step step, List<StepResult> results, Keywords keywords, StoryReporter reporter) {
            StepResult result = step.doNotPerform(reporter, getFailure());
            results.add(result);
            result.describeTo(reporter);
            return this;
        }

        @Override
        public UUIDExceptionWrapper getFailure() {
            return failure;
        }
    }

    private static final class Ignoring implements State {

        private final IgnoringStepsFailure failure;

        private Ignoring(IgnoringStepsFailure failure) {
            this.failure = failure;
        }

        @Override
        public State run(Step step, List<StepResult> results, Keywords keywords, StoryReporter reporter) {
            String stepAsString = step.asString(keywords);
            reporter.beforeStep(new org.jbehave.core.model.Step(StepExecutionType.IGNORABLE, stepAsString));
            StepResult result = AbstractStepResult.ignorable(stepAsString);
            results.add(result);
            result.describeTo(reporter);
            return this;
        }

        @Override
        public IgnoringStepsFailure getFailure() {
            return failure;
        }
    }

    public void perform(RunContext context, Story story) {
        boolean restartingStory = false;

        try {
            performCancellable(context, story);
            if (context.restartStory()) {
                context.reporter().restartedStory(story, context.failure(context.state()));
                restartingStory = true;
                perform(context, story);
            }
        } catch (InterruptedException e) {
            if (context.isCancelled(story)) {
                context.reporter().storyCancelled(story, context.storyDuration(story));
                context.reporter().afterStory(context.givenStory);
            }
            throw new UUIDExceptionWrapper(e);
        } finally {
            if (!context.givenStory() && !restartingStory) {
                invokeDelayedReporters(context.reporter());
            }
        }
    }

    private void performCancellable(RunContext context, Story story) throws InterruptedException {
        if (context.configuration().storyControls().resetStateBeforeStory()) {
            context.resetState();
            context.resetFailures(story);
        }

        if (!story.getPath().equals(context.path())) {
            context.currentPath(story.getPath());
        }

        if (context.configuration.dryRun()) {
            context.reporter().dryRun();
        }

        root.get(story).perform(context);
        if (context.failureOccurred()) {
            context.addFailure(story);
        }
    }

    public void performBeforeOrAfterStories(RunContext context, Stage stage) {
        String storyPath = StringUtils.capitalize(stage.name().toLowerCase()) + "Stories";
        context.currentPath(storyPath);
        context.reporter().beforeStoriesSteps(stage);
        try {
            (stage == Stage.BEFORE ? root.beforeSteps : root.afterSteps).perform(context);
        } catch (InterruptedException e) {
            throw new UUIDExceptionWrapper(e);
        } finally {
            context.reporter().afterStoriesSteps(stage);
            invokeDelayedReporters(context.reporter());
        }
    }

    private void invokeDelayedReporters(StoryReporter reporter) {
        if (reporter instanceof ConcurrentStoryReporter) {
            ((ConcurrentStoryReporter) reporter).invokeDelayed();
        } else if (reporter instanceof DelegatingStoryReporter) {
            for (StoryReporter delegate : ((DelegatingStoryReporter) reporter).getDelegates()) {
                invokeDelayedReporters(delegate);
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * The context for running a story.
     */
    public static class RunContext {
        private final Configuration configuration;
        private final boolean givenStory;

        private final AllStepCandidates allStepCandidates;
        private final EmbedderMonitor embedderMonitor;
        private final MetaFilter filter;
        private final BatchFailures failures;
        private final StepsContext stepsContext;
        private final Map<Story, StoryDuration> cancelledStories = new HashMap<>();
        private final Map<String, List<PendingStep>> pendingStories = new HashMap<>();
        private final ThreadLocal<StoryRunContext> storyRunContext = ThreadLocal.withInitial(StoryRunContext::new);

        public RunContext(Configuration configuration, AllStepCandidates allStepCandidates,
                EmbedderMonitor embedderMonitor, MetaFilter filter, BatchFailures failures) {
            this(configuration, allStepCandidates, embedderMonitor, filter, failures, false);
        }

        private RunContext(Configuration configuration, AllStepCandidates allStepCandidates,
                EmbedderMonitor embedderMonitor, MetaFilter filter, BatchFailures failures, boolean givenStory) {
            this.configuration = configuration;
            this.givenStory = givenStory;
            this.allStepCandidates = allStepCandidates;
            this.embedderMonitor = embedderMonitor;
            this.filter = filter;
            this.failures = failures;
            this.stepsContext = configuration.stepsContext();
            resetState();
        }

        public StepsContext stepsContext() {
            return stepsContext;
        }

        public boolean restartScenario() {
            Throwable cause = failure(state());
            while (cause != null) {
                if (cause instanceof RestartingScenarioFailure) {
                    return true;
                }
                cause = cause.getCause();
            }
            return false;
        }

        public boolean restartStory() {
            Throwable cause = failure(state());
            while (cause != null) {
                if (cause instanceof RestartingStoryFailure) {
                    return true;
                }
                cause = cause.getCause();
            }
            return false;
        }

        public void currentPath(String path) {
            currentRunContext().pathIs(path);
            currentRunContext().reporterIs(configuration.storyReporter(path));
        }

        public void interruptIfCancelled() throws InterruptedException {
            for (Story story : cancelledStories.keySet()) {
                if (path().equals(story.getPath())) {
                    throw new InterruptedException(path());
                }
            }
        }

        public boolean dryRun() {
            return configuration.storyControls().dryRun();
        }

        public Configuration configuration() {
            return configuration;
        }

        public boolean givenStory() {
            return givenStory;
        }

        public String path() {
            return currentRunContext().path();
        }

        public FilteredStory filter(Story story) {
            return new FilteredStory(filter, story, configuration.storyControls(), givenStory());
        }

        public MetaFilter filter() {
            return filter;
        }

        public PerformableSteps beforeStoriesSteps() {
            return new PerformableSteps(configuration.stepCollector().collectBeforeOrAfterStoriesSteps(
                    allStepCandidates.getBeforeStoriesSteps()));
        }

        public PerformableSteps afterStoriesSteps() {
            return new PerformableSteps(configuration.stepCollector().collectBeforeOrAfterStoriesSteps(
                    allStepCandidates.getAfterStoriesSteps()));
        }

        public PerformableSteps beforeStorySteps(Meta storyMeta) {
            return new PerformableSteps(configuration.stepCollector()
                    .collectBeforeOrAfterStorySteps(allStepCandidates.getBeforeStorySteps(givenStory), storyMeta));
        }

        public PerformableSteps afterStorySteps(Meta storyMeta) {
            return new PerformableSteps(configuration.stepCollector()
                    .collectBeforeOrAfterStorySteps(allStepCandidates.getAfterStorySteps(givenStory), storyMeta));
        }

        public PerformableSteps beforeScenarioSteps(Meta storyAndScenarioMeta, ScenarioType type) {
            return new PerformableSteps(configuration.stepCollector()
                    .collectBeforeScenarioSteps(allStepCandidates.getBeforeScenarioSteps(type), storyAndScenarioMeta));
        }

        public PerformableSteps afterScenarioSteps(Meta storyAndScenarioMeta, ScenarioType type) {
            return new PerformableSteps(configuration.stepCollector()
                    .collectAfterScenarioSteps(allStepCandidates.getAfterScenarioSteps(type), storyAndScenarioMeta));
        }

        private Map<Stage, PerformableSteps> lifecycleSteps(Lifecycle lifecycle, Meta meta, Scope scope) {
            MatchingStepMonitor monitor = new MatchingStepMonitor(configuration.stepMonitor());
            Map<Stage, List<Step>> steps = configuration.stepCollector().collectLifecycleSteps(
                    allStepCandidates.getRegularSteps(), lifecycle, meta, scope, monitor);
            Map<Stage, PerformableSteps> performableSteps = new EnumMap<>(Stage.class);
            for (Map.Entry<Stage, List<Step>> entry : steps.entrySet()) {
                performableSteps.put(entry.getKey(), new PerformableSteps(entry.getValue(), monitor.matched()));
            }
            return performableSteps;
        }

        private PerformableSteps scenarioSteps(Lifecycle lifecycle, Meta meta, Scenario scenario,
                Map<String, String> parameters) {
            MatchingStepMonitor monitor = new MatchingStepMonitor(configuration.stepMonitor());
            StepCollector stepCollector = configuration.stepCollector();
            Map<Stage, List<Step>> beforeOrAfterStepSteps = stepCollector.collectLifecycleSteps(
                    allStepCandidates.getRegularSteps(), lifecycle, meta, Scope.STEP, monitor);
            List<Step> steps = new LinkedList<>();
            for (Step step : stepCollector.collectScenarioSteps(allStepCandidates.getRegularSteps(), scenario,
                    parameters, monitor)) {
                steps.addAll(beforeOrAfterStepSteps.get(Stage.BEFORE));
                steps.add(step);
                steps.addAll(beforeOrAfterStepSteps.get(Stage.AFTER));
            }
            return new PerformableSteps(steps, monitor.matched());
        }

        public RunContext childContextFor(GivenStory givenStory) {
            RunContext child = new RunContext(configuration, allStepCandidates, embedderMonitor, filter,
                    failures, true);
            child.currentRunContext().pathIs(configuration.pathCalculator().calculate(path(), givenStory.getPath()));
            return child;
        }

        public void cancelStory(Story story, StoryDuration storyDuration) {
            cancelledStories.put(story, storyDuration);
        }

        public boolean isCancelled(Story story) {
            return cancelledStories.containsKey(story);
        }

        public StoryDuration storyDuration(Story story) {
            return cancelledStories.get(story);
        }

        public State state() {
            return currentRunContext().state();
        }

        public void stateIs(State state) {
            currentRunContext().stateIs(state);
        }

        public boolean failureOccurred() {
            return failed(state());
        }

        public void resetState() {
            currentRunContext().resetState();
        }

        /**
         * Reset all the existing failures.
         */
        public void resetFailures() {
            this.failures.clear();
        }

        /**
         * Resets only the failures corresponding to the given story.
         * @param story the story for which we want to remove the failures.
         */
        public void resetFailures(Story story) {
            this.failures.entrySet()
                    .removeIf(entry -> entry.getKey().equals(toBatchFailuresKey(story, entry.getValue())));
        }

        public StoryReporter reporter() {
            return currentRunContext().reporter();
        }

        public boolean failed(State state) {
            return !state.getClass().equals(FineSoFar.class);
        }

        public Throwable failure(State state) {
            if (failed(state)) {
                return state.getFailure().getCause();
            }
            return null;
        }

        public void addFailure(Story story) {
            addFailure(story, failure(state()));
        }

        public void addFailure(Story story, Throwable cause) {
            if (cause != null) {
                failures.put(toBatchFailuresKey(story, cause), cause);
            }
        }

        public void pendingSteps(List<PendingStep> pendingSteps) {
            if (!pendingSteps.isEmpty()) {
                pendingStories.put(path(), pendingSteps);
            }
        }

        public boolean hasPendingSteps() {
            return pendingStories.containsKey(path());
        }

        public boolean isStoryPending() {
            return pendingStories.containsKey(path());
        }

        public boolean hasFailed() {
            return failed(state());
        }

        public Status status(State initial) {
            if (isStoryPending()) {
                return Status.PENDING;
            } else if (failed(initial)) {
                return Status.NOT_PERFORMED;
            } else {
                return (hasFailed() ? Status.FAILED : Status.SUCCESSFUL);
            }
        }

        public MetaFilter getFilter() {
            return filter;
        }

        public BatchFailures getFailures() {
            return failures;
        }
        
        public EmbedderMonitor embedderMonitor() {
            return embedderMonitor;
        }

        private StoryRunContext currentRunContext() {
            return storyRunContext.get();
        }

        /**
         * Converts the given story and failure to the key to use to store the failure.
         * @param story the story where the failure occurred.
         * @param cause the failure that occurred.
         * @return the key to use to store the failure into the {@code BatchFailures}.
         */
        private String toBatchFailuresKey(Story story, Throwable cause) {
            return String.format("%s@%s", story.getPath(), Integer.toHexString(cause.hashCode()));
        }
    }

    private static class StoryRunContext {
        private State state;
        private String path;
        private StoryReporter reporter;

        private State state() {
            return state;
        }

        private void stateIs(State state) {
            this.state = state;
        }

        private void resetState() {
            this.state = new FineSoFar();
        }

        private String path() {
            return path;
        }

        private void pathIs(String path) {
            this.path = path;
        }

        public StoryReporter reporter() {
            return this.reporter;
        }

        private void reporterIs(StoryReporter reporter) {
            this.reporter = reporter;
        }
    }

    public static class FailureContext {

        List<Throwable> failures = new ArrayList<>();

        public void addFailure(Throwable failure) {
            failures.add(failure);
        }

        public List<Throwable> getFailures() {
            return failures;
        }

    }
    
    public static interface Performable {

        void perform(RunContext context) throws InterruptedException;

        void reportFailures(FailureContext context);

    }

    public static class PerformableRoot {

        private PerformableSteps beforeSteps = new PerformableSteps();
        private Map<String, PerformableStory> stories = new LinkedHashMap<>();
        private PerformableSteps afterSteps = new PerformableSteps();

        public void addBeforeSteps(PerformableSteps beforeSteps) {
            this.beforeSteps = beforeSteps;
        }

        public void add(PerformableStory performableStory) {
            stories.put(performableStory.getStory().getPath(), performableStory);
        }

        public void addAfterSteps(PerformableSteps afterSteps) {
            this.afterSteps = afterSteps;
        }

        public PerformableStory get(Story story) {
            PerformableStory performableStory = stories.get(story.getPath());
            if (performableStory != null) {
                return performableStory;
            }
            throw new RuntimeException("No performable story for path " + story.getPath());
        }

        public List<PerformableStory> getStories() {
            return new ArrayList<>(stories.values());
        }

    }

    public static enum Status {
        SUCCESSFUL, FAILED, PENDING, NOT_PERFORMED, EXCLUDED;
    }

    public abstract static class PerformableEntity implements Performable {

        private PerformableGivenStories givenStories;
        private final Map<Stage, Map<ExecutionType, PerformableSteps>> stageSteps;
        private final LifecycleStepsExecutionHook beforeHook;
        private final LifecycleStepsExecutionHook afterHook;

        public PerformableEntity(LifecycleStepsExecutionHook beforeHook, LifecycleStepsExecutionHook afterHook) {
            this.givenStories = new PerformableGivenStories(Collections.emptyList(), null);
            this.stageSteps = new EnumMap<>(Stage.class);
            this.beforeHook = beforeHook;
            this.afterHook = afterHook;
        }

        public void setGivenStories(PerformableGivenStories givenStories) {
            this.givenStories = givenStories;
        }

        public void addBeforeSteps(Lifecycle.ExecutionType type, PerformableSteps beforeSteps) {
            getBeforeSteps(type).add(beforeSteps);
        }

        public void addAfterSteps(Lifecycle.ExecutionType type, PerformableSteps afterSteps) {
            getAfterSteps(type).add(afterSteps);
        }

        protected void performBeforeSteps(RunContext context) throws InterruptedException {
            performSteps(context, Stage.BEFORE, ExecutionType.SYSTEM);
            performSteps(context, Stage.BEFORE, ExecutionType.USER);
        }

        protected void performAfterSteps(RunContext context) throws InterruptedException {
            performSteps(context, Stage.AFTER, ExecutionType.USER);
            performSteps(context, Stage.AFTER, ExecutionType.SYSTEM);
        }

        protected PerformableSteps getBeforeSteps(Lifecycle.ExecutionType type) {
            return getPerformableSteps(Stage.BEFORE, type);
        }

        protected PerformableSteps getAfterSteps(Lifecycle.ExecutionType type) {
            return getPerformableSteps(Stage.AFTER, type);
        }

        private PerformableSteps getPerformableSteps(Stage stage, Lifecycle.ExecutionType type) {
            Map<ExecutionType, PerformableSteps> steps = stageSteps.computeIfAbsent(stage,
                    t -> new EnumMap<>(ExecutionType.class));
            return steps.computeIfAbsent(type, s -> new PerformableSteps());
        }

        private void performSteps(RunContext context, Stage stage, Lifecycle.ExecutionType type)
                throws InterruptedException {
            StoryReporter reporter = context.reporter();
            this.beforeHook.perform(reporter, stage, type);
            getPerformableSteps(stage, type).perform(context);
            this.afterHook.perform(reporter, stage, type);
        }

        public PerformableGivenStories getGivenStories() {
            return givenStories;
        }

    }

    private interface LifecycleStepsExecutionHook {
        void perform(StoryReporter reporter, Stage stage, Lifecycle.ExecutionType type);
    }

    public static class PerformableStory extends PerformableEntity {

        private final Story story;
        private final transient Keywords keywords;
        private final boolean givenStory;
        private boolean excluded;
        private Status status;
        private Timing timing = new Timing();
        private List<PerformableScenario> scenarios = new ArrayList<>();

        public PerformableStory(Story story, Keywords keywords, boolean givenStory) {
            super((reporter, type, stage) -> reporter.beforeStorySteps(type, stage),
                    (reporter, type, stage) -> reporter.afterStorySteps(type, stage));
            this.story = story;
            this.keywords = keywords;
            this.givenStory = givenStory;
        }

        public void excluded(boolean excluded) {
            this.excluded = excluded;
        }

        public boolean isExcluded() {
            return excluded;
        }

        public Story getStory() {
            return story;
        }

        public Keywords getKeywords() {
            return keywords;
        }

        public boolean givenStory() {
            return givenStory;
        }

        public Status getStatus() {
            return status;
        }

        public Timing getTiming() {
            return timing;
        }

        public void add(PerformableScenario performableScenario) {
            scenarios.add(performableScenario);
        }

        public void addAll(List<PerformableScenario> performableScenarios) {
            scenarios.addAll(performableScenarios);
        }

        @Override
        public void perform(RunContext context) throws InterruptedException {
            if (isExcluded()) {
                context.reporter().storyExcluded(story, context.filter.asString());
                this.status = Status.EXCLUDED;
            }
            Timer timer = new Timer().start();
            try {
                context.stepsContext().resetStory();
                context.reporter().beforeStory(story, givenStory);
                context.reporter().narrative(story.getNarrative());
                context.reporter().lifecycle(story.getLifecycle());
                State state = context.state();
                performBeforeSteps(context);
                getGivenStories().perform(context);
                if (!context.failureOccurred() || !context.configuration().storyControls()
                        .skipStoryIfGivenStoryFailed()) {
                    performScenarios(context);
                }
                performAfterSteps(context);
                context.configuration().storyControls().resetCurrentStoryControls();
                if (context.restartStory()) {
                    context.reporter().afterStory(true);
                } else {
                    context.reporter().afterStory(givenStory);
                }
                this.status = context.status(state);
            } finally {
                timing = new Timing(timer.stop());
            }
        }

        @Override
        public void reportFailures(FailureContext context) {
            for (PerformableScenario scenario : scenarios) {
                scenario.reportFailures(context);
            }
        }

        private void performScenarios(RunContext context) throws InterruptedException {
            context.reporter().beforeScenarios();
            for (PerformableScenario scenario : scenarios) {
                scenario.perform(context);
            }
            context.reporter().afterScenarios();
        }

        public List<PerformableScenario> getScenarios() {
            return scenarios;
        }

        public boolean hasIncludedScenarios() {
            return getScenarios().stream().anyMatch(scenario -> !scenario.isExcluded());
        }
    }

    public static class PerformableScenario implements Performable {

        private final Scenario scenario;
        private final String storyPath;
        private boolean excluded;
        @SuppressWarnings("unused")
        private Status status;
        private Timing timing = new Timing();
        private NormalPerformableScenario normalScenario;
        private List<ExamplePerformableScenario> exampleScenarios;

        public PerformableScenario(Scenario scenario, String storyPath) {
            this.scenario = scenario;
            this.storyPath = storyPath;
        }

        public void useNormalScenario(NormalPerformableScenario normalScenario) {
            this.normalScenario = normalScenario;
        }

        public void addExampleScenario(ExamplePerformableScenario exampleScenario) {
            if (exampleScenarios == null) {
                exampleScenarios = new ArrayList<>();
            }
            exampleScenarios.add(exampleScenario);
        }

        public void excluded(boolean excluded) {
            this.excluded = excluded;
        }

        public boolean isExcluded() {
            return excluded;
        }

        public Status getStatus() {
            return status;
        }

        public Timing getTiming() {
            return timing;
        }

        public Scenario getScenario() {
            return scenario;
        }

        public String getStoryPath() {
            return storyPath;
        }

        public Throwable getFailure() {
            FailureContext context = new FailureContext();
            reportFailures(context);
            List<Throwable> failures = context.getFailures();
            if (failures.size() > 0) {
                return failures.get(0);
            }
            return null;
        }

        public boolean hasNormalScenario() {
            return normalScenario != null;
        }

        public boolean hasExamples() {
            return exampleScenarios != null && exampleScenarios.size() > 0;
        }

        public boolean isPerformable() {
            return hasNormalScenario() || hasExamples() || isExcluded();
        }

        public List<ExamplePerformableScenario> getExamples() {
            return exampleScenarios;
        }

        @Override
        public void perform(RunContext context) throws InterruptedException {
            if (isExcluded()) {
                context.embedderMonitor().scenarioExcluded(scenario, context.filter());
                return;
            }
            Timer timer = new Timer().start();
            try {
                context.stepsContext().resetScenario();
                context.reporter().beforeScenario(scenario);
                State state = context.state();
                if (hasExamples()) {
                    context.reporter().beforeExamples(scenario.getSteps(),
                            scenario.getExamplesTable());
                    for (ExamplePerformableScenario exampleScenario : exampleScenarios) {
                        exampleScenario.perform(context);
                    }
                    context.reporter().afterExamples();
                } else {
                    context.stepsContext().resetExample();
                    normalScenario.perform(context);
                }
                this.status = context.status(state);
            } finally {
                timing = new Timing(timer.stop());
                context.reporter().afterScenario(timing);
            }
        }

        @Override
        public void reportFailures(FailureContext context) {
            if (hasExamples()) {
                for (ExamplePerformableScenario exampleScenario : exampleScenarios) {
                    exampleScenario.reportFailures(context);
                }
            } else {
                normalScenario.reportFailures(context);
            }
        }

    }

    public abstract static class AbstractPerformableScenario extends PerformableEntity {

        private transient Story story;
        protected final Map<String, String> parameters;
        protected final PerformableSteps steps = new PerformableSteps();
        private Meta storyAndScenarioMeta = new Meta();

        public AbstractPerformableScenario(Story story, Scenario scenario) {
            this(story, scenario, new HashMap<>());
        }

        public AbstractPerformableScenario(Story story, Scenario scenario, Map<String, String> parameters) {
            super((reporter, type, stage) -> reporter.beforeScenarioSteps(type, stage),
                    (reporter, type, stage) -> reporter.afterScenarioSteps(type, stage));
            this.story = story;
            this.parameters = parameters;
        }

        public void addSteps(PerformableSteps steps) {
            this.steps.add(steps);
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        protected void performRestartableSteps(RunContext context)
                throws InterruptedException {
            boolean restart = true;
            while (restart) {
                restart = false;
                try {
                    perform(steps, context, r -> r.beforeScenarioSteps(null, null),
                            r -> r.afterScenarioSteps(null, null));
                } catch (RestartingScenarioFailure e) {
                    restart = true;
                    continue;
                }
            }
        }

        protected void performScenario(RunContext context) throws InterruptedException {
            performBeforeSteps(context);
            getGivenStories().perform(context);
            performRestartableSteps(context);
            performAfterSteps(context);
        }

        protected void perform(PerformableSteps performableSteps, RunContext context, Consumer<StoryReporter> before,
                Consumer<StoryReporter> after) throws InterruptedException {
            StoryReporter reporter = context.reporter();
            before.accept(reporter);
            performableSteps.perform(context);
            after.accept(reporter);
        }

        @Override
        public void reportFailures(FailureContext context) {
            getBeforeSteps(ExecutionType.SYSTEM).reportFailures(context);
            getBeforeSteps(ExecutionType.USER).reportFailures(context);
            steps.reportFailures(context);
            getAfterSteps(ExecutionType.USER).reportFailures(context);
            getAfterSteps(ExecutionType.SYSTEM).reportFailures(context);
        }

        protected void resetStateIfConfigured(RunContext context) {
            if (context.configuration().storyControls().resetStateBeforeScenario()) {
                if (context.failureOccurred()) {
                    context.addFailure(story);
                }
                context.resetState();
            }
        }

        public Meta getStoryAndScenarioMeta() {
            return storyAndScenarioMeta;
        }

        public void setStoryAndScenarioMeta(Meta storyAndScenarioMeta) {
            this.storyAndScenarioMeta = storyAndScenarioMeta;
        }
    }

    public static class NormalPerformableScenario extends AbstractPerformableScenario {

        public NormalPerformableScenario(Story story, Scenario scenario) {
            super(story, scenario);
        }

        @Override
        public void perform(RunContext context) throws InterruptedException {
            resetStateIfConfigured(context);
            performScenario(context);
        }
    }

    public static class ExamplePerformableScenario extends AbstractPerformableScenario {

        private final int exampleIndex;

        public ExamplePerformableScenario(Story story, Scenario scenario, Map<String, String> exampleParameters,
                int exampleIndex) {
            super(story, scenario, exampleParameters);
            this.exampleIndex = exampleIndex;
        }

        @Override
        public void perform(RunContext context) throws InterruptedException {
            Meta parameterMeta = parameterMeta(context.configuration().keywords(), parameters).inheritFrom(
                    getStoryAndScenarioMeta());
            if (parameterMeta.isEmpty() || !context.filter().excluded(parameterMeta)) {
                resetStateIfConfigured(context);
                context.stepsContext().resetExample();
                context.reporter().example(parameters, exampleIndex);
                performScenario(context);
            }
        }

        private Meta parameterMeta(Keywords keywords, Map<String, String> parameters) {
            String meta = keywords.meta();
            if (parameters.containsKey(meta)) {
                return Meta.createMeta(parameters.get(meta), keywords);
            }
            return Meta.EMPTY;
        }

    }

    public static class PerformableGivenStories implements Performable {

        private final List<PerformableStory> performableGivenStories;
        private final GivenStories givenStories;

        public PerformableGivenStories(List<PerformableStory> performableGivenStories, GivenStories givenStories) {
            this.performableGivenStories = performableGivenStories;
            this.givenStories = givenStories;
        }

        @Override
        public void perform(RunContext context) throws InterruptedException {
            if (performableGivenStories.size() > 0) {
                StoryReporter storyReporter = context.reporter();
                storyReporter.beforeGivenStories();
                storyReporter.givenStories(givenStories);
                for (PerformableStory story : performableGivenStories) {
                    story.perform(context);
                }
                storyReporter.afterGivenStories();
            }
        }

        @Override
        public void reportFailures(FailureContext context) {
        }
    }

    public static class PerformableSteps implements Performable {

        private final transient List<Step> steps;
        private final transient List<PendingStep> pendingSteps;
        private List<StepMatch> matches;
        private List<StepResult> results;

        public PerformableSteps() {
            this(null);
        }

        public PerformableSteps(List<Step> steps) {
            this(steps, null);
        }

        public PerformableSteps(List<Step> steps, List<StepMatch> stepMatches) {
            this.steps = steps != null ? steps : new ArrayList<Step>();
            this.pendingSteps = pendingSteps();
            this.matches = stepMatches;
        }

        public void add(PerformableSteps performableSteps) {
            this.steps.addAll(performableSteps.steps);
            this.pendingSteps.addAll(performableSteps.pendingSteps);
            if (performableSteps.matches != null) {
                if (this.matches == null) {
                    this.matches = new ArrayList<>();
                }
                this.matches.addAll(performableSteps.matches);
            }
        }
        
        @Override
        public void perform(RunContext context) throws InterruptedException {
            if (steps.size() == 0) {
                return;
            }
            Keywords keywords = context.configuration().keywords();
            State state = context.state();
            State originalState = state;
            StoryReporter reporter = context.reporter();
            results = new ArrayList<>();
            for (Step step : steps) {
                try {
                    context.interruptIfCancelled();
                    state = state.run(step, results, keywords, reporter);
                } catch (RestartingScenarioFailure e) {
                    reporter.restarted(step.asString(keywords), e);
                    throw e;
                }
            }
            context.stateIs(state instanceof Ignoring ? originalState : state);
            context.pendingSteps(pendingSteps);
            generatePendingStepMethods(context, pendingSteps);
        }

        @Override
        public void reportFailures(FailureContext context) {
            // Results can be null if the steps are not executed
            if (results == null) {
                return;
            }
            for (StepResult result : results) {
                if (result instanceof AbstractStepResult.Failed) {
                    context.addFailure(result.getFailure());
                }
            }
        }

        private List<PendingStep> pendingSteps() {
            List<PendingStep> pending = new ArrayList<>();
            for (Step step : steps) {
                if (step instanceof PendingStep) {
                    pending.add((PendingStep) step);
                }
            }
            return pending;
        }

        private void generatePendingStepMethods(RunContext context, List<PendingStep> pendingSteps) {
            if (!pendingSteps.isEmpty()) {
                PendingStepMethodGenerator generator = new PendingStepMethodGenerator(context.configuration()
                        .keywords());
                List<String> methods = new ArrayList<>();
                for (PendingStep pendingStep : pendingSteps) {
                    if (!pendingStep.annotated()) {
                        methods.add(generator.generateMethod(pendingStep));
                    }
                }
                context.reporter().pendingMethods(methods);
                if (context.configuration().pendingStepStrategy() instanceof FailingUponPendingStep) {
                    throw new PendingStepsFound(pendingSteps);
                }
            }
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }

    public RunContext newRunContext(Configuration configuration, AllStepCandidates allStepCandidates,
            EmbedderMonitor embedderMonitor, MetaFilter filter, BatchFailures failures) {
        return new RunContext(configuration, allStepCandidates, embedderMonitor, filter, failures);
    }
}
