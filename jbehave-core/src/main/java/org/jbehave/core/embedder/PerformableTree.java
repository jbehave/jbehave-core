package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.PendingStepMethodGenerator;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.ParameterisedStep;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepResult;

/**
 * Creates a tree of {@link Performable} objects.
 */
public class PerformableTree {

    private PerformableRoot root = new PerformableRoot();

    public PerformableRoot getRoot() {
        return root;
    }

    public void addStories(RunContext context, List<String> storyPaths) {
        root.addBeforeSteps(context.beforeOrAfterStoriesSteps(Stage.BEFORE));
        for (String storyPath : storyPaths) {
            root.add(performableStory(context, storyOfPath(context.configuration, storyPath),
                    new HashMap<String, String>()));
        }
        root.addAfterSteps(context.beforeOrAfterStoriesSteps(Stage.AFTER));
    }

    public PerformableStory performableStory(RunContext context, Story story, Map<String, String> storyParameters) {
        PerformableStory performableStory = new PerformableStory(story);

        // determine if story is allowed
        boolean storyAllowed = true;
        FilteredStory filteredStory = context.filter(story);
        Meta storyMeta = story.getMeta();
        if (!filteredStory.allowed()) {
            storyAllowed = false;
        }

        performableStory.allowed(storyAllowed);

        if (storyAllowed) {

            performableStory.addBeforeSteps(context.beforeOrAfterStorySteps(story, Stage.BEFORE));

            // determine if before and after scenario steps should be run
            boolean runBeforeAndAfterScenarioSteps = shouldRunBeforeOrAfterScenarioSteps(context);

            for (Scenario scenario : story.getScenarios()) {
                performableStory.add(performableScenario(context, story, storyParameters, filteredStory, storyMeta,
                        runBeforeAndAfterScenarioSteps, scenario));
            }

            performableStory.addAfterSteps(context.beforeOrAfterStorySteps(story, Stage.AFTER));

        }

        return performableStory;
    }

    private PerformableScenario performableScenario(RunContext context, Story story,
            Map<String, String> storyParameters, FilteredStory filterContext, Meta storyMeta,
            boolean runBeforeAndAfterScenarioSteps, Scenario scenario) {
        PerformableScenario performableScenario = new PerformableScenario(scenario);
        // scenario also inherits meta from story
        boolean scenarioAllowed = true;
        if (failureOccurred(context) && context.configuration().storyControls().skipScenariosAfterFailure()) {
            return performableScenario;
        }

        if (!filterContext.allowed(scenario)) {
            scenarioAllowed = false;
        }

        performableScenario.allowed(scenarioAllowed);

        if (scenarioAllowed) {
            Meta storyAndScenarioMeta = scenario.getMeta().inheritFrom(storyMeta);
            // run before scenario steps, if allowed
            if (runBeforeAndAfterScenarioSteps) {
                performableScenario.addBeforeSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta,
                        Stage.BEFORE, ScenarioType.NORMAL));
            }

            if (isParameterisedByExamples(scenario)) {
                ExamplesTable table = scenario.getExamplesTable();
                for (Map<String, String> scenarioParameters : table.getRows()) {
                    performableScenario.addExampleScenario(exampleScenario(context, story, scenario,
                            storyAndScenarioMeta, scenarioParameters));
                }
            } else { // plain old scenario
                addMetaParameters(storyParameters, storyAndScenarioMeta);
                performableScenario.addGivenStories(performableGivenStories(context, story, scenario, storyParameters));
                performableScenario.addSteps(context.scenarioSteps(scenario, storyParameters));
            }

            // after scenario steps, if allowed
            if (runBeforeAndAfterScenarioSteps) {
                performableScenario.addAfterSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.AFTER,
                        ScenarioType.NORMAL));
            }

        }
        return performableScenario;
    }

    private PerformableExampleScenario exampleScenario(RunContext context, Story story, Scenario scenario,
            Meta storyAndScenarioMeta, Map<String, String> scenarioParameters) {
        PerformableExampleScenario exampleScenario = new PerformableExampleScenario(scenarioParameters);
        exampleScenario.addBeforeSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.BEFORE,
                ScenarioType.EXAMPLE));
        addMetaParameters(scenarioParameters, storyAndScenarioMeta);
        exampleScenario.addGivenStories(performableGivenStories(context, story, scenario, scenarioParameters));
        exampleScenario.addSteps(context.scenarioSteps(scenario, scenarioParameters));
        exampleScenario.addAfterSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.AFTER,
                ScenarioType.EXAMPLE));
        return exampleScenario;
    }

    private List<PerformableStory> performableGivenStories(RunContext context, Story story, Scenario scenario,
            Map<String, String> scenarioParameters) {
        List<PerformableStory> stories = new ArrayList<PerformableStory>();
        GivenStories givenStories = scenario.getGivenStories();
        if (givenStories.getPaths().size() > 0) {
            for (GivenStory givenStory : givenStories.getStories()) {
                RunContext childContext = context.childContextFor(givenStory);
                // run given story, using any parameters provided
                Story storyOfPath = storyOfPath(context.configuration(), childContext.path());
                scenarioParameters.putAll(givenStory.getParameters());
                stories.add(performableStory(childContext, storyOfPath, scenarioParameters));
            }
        }
        return stories;
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
            storyParameters.put(name, meta.getProperty(name));
        }
    }

    private boolean shouldRunBeforeOrAfterScenarioSteps(RunContext context) {
        Configuration configuration = context.configuration();
        if (!configuration.storyControls().skipBeforeAndAfterScenarioStepsIfGivenStory()) {
            return true;
        }

        return !context.givenStory();
    }

    private boolean failureOccurred(RunContext context) {
        return context.failureOccurred();
    }

    private boolean isParameterisedByExamples(Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() > 0 && !scenario.getGivenStories().requireParameters();
    }

    static void generatePendingStepMethods(RunContext context, List<Step> steps) {
        List<PendingStep> pendingSteps = new ArrayList<PendingStep>();
        for (Step step : steps) {
            if (step instanceof PendingStep) {
                pendingSteps.add((PendingStep) step);
            }
        }
        if (!pendingSteps.isEmpty()) {
            PendingStepMethodGenerator generator = new PendingStepMethodGenerator(context.configuration().keywords());
            List<String> methods = new ArrayList<String>();
            for (PendingStep pendingStep : pendingSteps) {
                if (!pendingStep.annotated()) {
                    methods.add(generator.generateMethod(pendingStep));
                }
            }
        }
    }

    public interface State {
        State run(Step step, StoryReporter reporter, UUIDExceptionWrapper storyFailureIfItHappened);
    }

    private final static class FineSoFar implements State {

        public State run(Step step, StoryReporter reporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            if (step instanceof ParameterisedStep) {
                ((ParameterisedStep) step).describeTo(reporter);
            }
            StepResult result = step.perform(storyFailureIfItHappened);
            result.describeTo(reporter);
            UUIDExceptionWrapper stepFailure = result.getFailure();
            if (stepFailure == null) {
                return this;
            }

            mostImportantOf(storyFailureIfItHappened, stepFailure);
            return new SomethingHappened(stepFailure);
        }

        private UUIDExceptionWrapper mostImportantOf(UUIDExceptionWrapper failure1, UUIDExceptionWrapper failure2) {
            return failure1 == null ? failure2
                    : failure1.getCause() instanceof PendingStepFound ? (failure2 == null ? failure1 : failure2)
                            : failure1;
        }

    }

    private final static class SomethingHappened implements State {
        UUIDExceptionWrapper scenarioFailure;

        public SomethingHappened(UUIDExceptionWrapper scenarioFailure) {
            this.scenarioFailure = scenarioFailure;
        }

        public State run(Step step, StoryReporter reporter, UUIDExceptionWrapper storyFailure) {
            StepResult result = step.doNotPerform(storyFailure);
            result.describeTo(reporter);
            return this;
        }
    }

    public void perform(RunContext context, Story story) {
        try {
            performCancellable(context, story);
        } catch (Throwable e) {
            if (context.isCancelled(story)) {
                context.reporter().storyCancelled(story, context.storyDuration(story));
                context.reporter().afterStory(context.givenStory);
            }
            throw new UUIDExceptionWrapper(e);
        } finally {
            if (!context.givenStory() && context.reporter() instanceof ConcurrentStoryReporter) {
                ((ConcurrentStoryReporter) context.reporter()).invokeDelayed();
            }
        }
    }

    public void performCancellable(RunContext context, Story story) throws InterruptedException {
        if (context.configuration().storyControls().resetStateBeforeStory()) {
            context.resetState();
        }
        context.currentPath(story.getPath());

        root.get(story).perform(context);
        if (context.failureOccurred()) {
            context.addFailure();
        }
    }

    public void performBeforeOrAfterStories(RunContext context, Stage stage) {
        String storyPath = StringUtils.capitalize(stage.name().toLowerCase()) + "Stories";
        context.currentPath(storyPath);
        context.reporter().beforeStory(new Story(storyPath), false);
        try {
            (stage == Stage.BEFORE ? root.beforeSteps : root.afterSteps).perform(context);
        } catch (InterruptedException e) {
            throw new UUIDExceptionWrapper(e);
        } finally {
            if (context.reporter() instanceof ConcurrentStoryReporter) {
                ((ConcurrentStoryReporter) context.reporter()).invokeDelayed();
            }
        }
        context.reporter().afterStory(false);
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
        private final InjectableStepsFactory stepsFactory;
        private final List<CandidateSteps> candidateSteps;
        private final MetaFilter filter;
        private final BatchFailures failures;
        private Map<Story, StoryDuration> cancelledStories = new HashMap<Story, StoryDuration>();
        private String path;
        private boolean givenStory;
        private State state;
        private StoryReporter reporter;

        public RunContext(Configuration configuration, InjectableStepsFactory stepsFactory, MetaFilter filter,
                BatchFailures failures) {
            this.configuration = configuration;
            this.stepsFactory = stepsFactory;
            this.candidateSteps = stepsFactory.createCandidateSteps();
            this.filter = filter;
            this.failures = failures;
            resetState();
        }

        public void currentPath(String path) {
            this.path = path;
            this.reporter = configuration.storyReporter(path);
        }

        public void interruptIfCancelled() throws InterruptedException {
            for (Story story : cancelledStories.keySet()) {
                if (path.equals(story.getPath())) {
                    throw new InterruptedException(path);
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
            return path;
        }

        public FilteredStory filter(Story story) {
            return new FilteredStory(filter, story, configuration.storyControls());
        }

        public String metaFilterAsString() {
            return filter.asString();
        }

        public PerformableSteps beforeOrAfterStoriesSteps(Stage stage) {
            return new PerformableSteps(configuration.stepCollector().collectBeforeOrAfterStoriesSteps(candidateSteps,
                    stage));
        }

        public PerformableSteps beforeOrAfterStorySteps(Story story, Stage stage) {
            return new PerformableSteps(configuration.stepCollector().collectBeforeOrAfterStorySteps(candidateSteps,
                    story, stage, givenStory));
        }

        public PerformableSteps beforeOrAfterScenarioSteps(Meta storyAndScenarioMeta, Stage stage, ScenarioType type) {
            return new PerformableSteps(configuration.stepCollector().collectBeforeOrAfterScenarioSteps(candidateSteps,
                    storyAndScenarioMeta, stage, type));
        }

        public PerformableSteps scenarioSteps(Scenario scenario, Map<String, String> parameters) {
            return new PerformableSteps(configuration.stepCollector().collectScenarioSteps(candidateSteps, scenario,
                    parameters));
        }

        public RunContext childContextFor(GivenStory givenStory) {
            RunContext child = new RunContext(configuration, stepsFactory, filter, failures);
            child.path = configuration.pathCalculator().calculate(path, givenStory.getPath());
            child.givenStory = true;
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
            return state;
        }

        public void stateIs(State state) {
            this.state = state;
        }

        public boolean failureOccurred() {
            return failed(state);
        }

        public void resetState() {
            this.state = new FineSoFar();
        }

        public void resetFailures() {
            this.failures.clear();
        }

        public StoryReporter reporter() {
            return reporter;
        }

        public boolean failed(State state) {
            return !state.getClass().equals(FineSoFar.class);
        }

        public Throwable failure(State state) {
            if (failed(state)) {
                return ((SomethingHappened) state).scenarioFailure.getCause();
            }
            return null;
        }

        public void addFailure() {
            Throwable failure = failure(state);
            if (failure != null) {
                failures.put(state.toString(), failure);
            }
        }

        public void addFailure(String path, Throwable cause) {
            if (cause != null) {
                failures.put(path, cause);
            }
        }

        public MetaFilter getFilter() {
            return filter;
        }

        public BatchFailures getFailures() {
            return failures;
        }

    }

    public static interface Performable {

        void perform(RunContext context) throws InterruptedException;

    }

    public static class PerformableRoot {

        private PerformableSteps beforeSteps = new PerformableSteps();
        private List<PerformableStory> stories = new ArrayList<PerformableStory>();
        private PerformableSteps afterSteps = new PerformableSteps();

        public void addBeforeSteps(PerformableSteps beforeSteps) {
            this.beforeSteps = beforeSteps;
        }

        public void add(PerformableStory performableStory) {
            stories.add(performableStory);
        }

        public void addAfterSteps(PerformableSteps afterSteps) {
            this.afterSteps = afterSteps;
        }

        public PerformableStory get(Story story) {
            for (PerformableStory performableStory : stories) {
                if (performableStory.story.getPath().equals(story.getPath())) {
                    return performableStory;
                }
            }
            throw new RuntimeException("No performable story for path " + story.getPath());
        }

    }

    public static class PerformableStory implements Performable {

        private final Story story;
        private boolean allowed;
        private List<PerformableScenario> scenarios = new ArrayList<PerformableScenario>();
        private PerformableSteps beforeSteps = new PerformableSteps();
        private PerformableSteps afterSteps = new PerformableSteps();

        public PerformableStory(Story story) {
            this.story = story;
        }

        public void allowed(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public void addBeforeSteps(PerformableSteps beforeSteps) {
            this.beforeSteps = beforeSteps;
        }

        public void addAfterSteps(PerformableSteps afterSteps) {
            this.afterSteps = afterSteps;
        }

        public void add(PerformableScenario performableScenario) {
            scenarios.add(performableScenario);
        }

        public void perform(RunContext context) throws InterruptedException {
            context.reporter().narrative(story.getNarrative());
            if (!allowed) {
                context.reporter().storyNotAllowed(story, context.filter.asString());
            }
            context.reporter().beforeStory(story, context.givenStory);
            beforeSteps.perform(context);
            for (PerformableScenario scenario : scenarios) {
                scenario.perform(context);
            }
            afterSteps.perform(context);
            context.reporter().afterStory(context.givenStory);
        }

    }

    public static class PerformableScenario implements Performable {

        private final Scenario scenario;
        private boolean allowed;
        private List<PerformableExampleScenario> exampleScenarios = new ArrayList<PerformableExampleScenario>();
        private List<PerformableStory> performableGivenStories = new ArrayList<PerformableStory>();
        private PerformableSteps beforeSteps = new PerformableSteps();
        private PerformableSteps steps = new PerformableSteps();
        private PerformableSteps afterSteps = new PerformableSteps();

        public PerformableScenario(Scenario scenario) {
            this.scenario = scenario;
        }

        public void addGivenStories(List<PerformableStory> performableGivenStories) {
            this.performableGivenStories.addAll(performableGivenStories);
        }

        public void addExampleScenario(PerformableExampleScenario exampleScenario) {
            exampleScenarios.add(exampleScenario);
        }

        public void allowed(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public void addBeforeSteps(PerformableSteps beforeSteps) {
            this.beforeSteps = beforeSteps;
        }

        public void addSteps(PerformableSteps steps) {
            this.steps = steps;
        }

        public void addAfterSteps(PerformableSteps afterSteps) {
            this.afterSteps = afterSteps;
        }

        public void perform(RunContext context) throws InterruptedException {
            context.reporter().beforeScenario(scenario.getTitle());
            if (!exampleScenarios.isEmpty()) {
                context.reporter().beforeExamples(scenario.getSteps(), scenario.getExamplesTable());
                for (PerformableExampleScenario exampleScenario : exampleScenarios) {
                    exampleScenario.perform(context);
                }
                context.reporter().afterExamples();
            } else {
                if (context.configuration().storyControls().resetStateBeforeScenario()) {
                    context.resetState();
                }
                beforeSteps.perform(context);
                if (scenario.getGivenStories().getPaths().size() > 0) {
                    context.reporter().givenStories(scenario.getGivenStories());
                    for (PerformableStory story : performableGivenStories) {
                        story.perform(context);
                    }
                }
                steps.perform(context);
                afterSteps.perform(context);
            }
            context.reporter().afterScenario();
        }
    }

    public static class PerformableExampleScenario implements Performable {

        private final Map<String, String> exampleParameters;
        private List<PerformableStory> performableGivenStories = new ArrayList<PerformableStory>();
        private PerformableSteps beforeSteps = new PerformableSteps();
        private PerformableSteps steps = new PerformableSteps();
        private PerformableSteps afterSteps = new PerformableSteps();

        public PerformableExampleScenario(Map<String, String> exampleParameters) {
            this.exampleParameters = exampleParameters;
        }

        public void addGivenStories(List<PerformableStory> performableGivenStories) {
            this.performableGivenStories.addAll(performableGivenStories);
        }

        public void addBeforeSteps(PerformableSteps beforeSteps) {
            this.beforeSteps = beforeSteps;
        }

        public void addSteps(PerformableSteps steps) {
            this.steps = steps;
        }

        public void addAfterSteps(PerformableSteps afterSteps) {
            this.afterSteps = afterSteps;
        }

        public void perform(RunContext context) throws InterruptedException {
            if (context.configuration().storyControls().resetStateBeforeScenario()) {
                context.resetState();
            }
            context.reporter().example(exampleParameters);
            beforeSteps.perform(context);
            for (PerformableStory story : performableGivenStories) {
                story.perform(context);
            }
            steps.perform(context);
            afterSteps.perform(context);
        }
    }

    public static class PerformableSteps implements Performable {

        private final List<Step> steps;

        public PerformableSteps() {
            this(null);
        }

        public PerformableSteps(List<Step> steps) {
            this.steps = steps;
        }

        public void perform(RunContext context) throws InterruptedException {
            if (steps == null || steps.size() == 0) {
                return;
            }
            State state = context.state();
            StoryReporter reporter = context.reporter();
            for (Step step : steps) {
                context.interruptIfCancelled();
                state = state.run(step, reporter, null);
            }
            context.stateIs(state);
            generatePendingStepMethods(context, steps);
        }

        private void generatePendingStepMethods(RunContext context, List<Step> steps) {
            List<PendingStep> pendingSteps = new ArrayList<PendingStep>();
            for (Step step : steps) {
                if (step instanceof PendingStep) {
                    pendingSteps.add((PendingStep) step);
                }
            }
            if (!pendingSteps.isEmpty()) {
                PendingStepMethodGenerator generator = new PendingStepMethodGenerator(context.configuration().keywords());
                List<String> methods = new ArrayList<String>();
                for (PendingStep pendingStep : pendingSteps) {
                    if (!pendingStep.annotated()) {
                        methods.add(generator.generateMethod(pendingStep));
                    }
                }
                context.reporter().pendingMethods(methods);
            }
        }
        
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }

    public RunContext newRunContext(Configuration configuration, InjectableStepsFactory stepsFactory,
            MetaFilter filter, BatchFailures failures) {
        return new RunContext(configuration, stepsFactory, filter, failures);
    }

}
