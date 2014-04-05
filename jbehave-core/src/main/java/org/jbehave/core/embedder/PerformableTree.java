package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.MatchingStepMonitor.StepMatch;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepsFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Lifecycle;
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
import org.jbehave.core.steps.Timer;

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

    private static final Map<String, String> NO_PARAMETERS = new HashMap<String, String>();

    private PerformableRoot root = new PerformableRoot();

    public PerformableRoot getRoot() {
        return root;
    }

    public void addStories(RunContext context, List<String> storyPaths) {
        root.addBeforeSteps(context.beforeOrAfterStoriesSteps(Stage.BEFORE));
        for (String storyPath : storyPaths) {
            root.add(performableStory(context, storyOfPath(context.configuration(), storyPath), NO_PARAMETERS));
        }
        root.addAfterSteps(context.beforeOrAfterStoriesSteps(Stage.AFTER));
    }

    private PerformableStory performableStory(RunContext context, Story story, Map<String, String> storyParameters) {
        PerformableStory performableStory = new PerformableStory(story, context.configuration().keywords(),
                context.givenStory());

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

            performableStory
                    .addGivenStories(performableGivenStories(context, story.getGivenStories(), storyParameters));

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
        PerformableScenario performableScenario = new PerformableScenario(scenario, story.getPath());
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
            Lifecycle lifecycle = story.getLifecycle();

            Meta storyAndScenarioMeta = scenario.getMeta().inheritFrom(storyMeta);
			NormalPerformableScenario normalScenario = normalScenario(
					context, lifecycle, scenario, storyAndScenarioMeta,
					storyParameters);

            // run before scenario steps, if allowed
            if (runBeforeAndAfterScenarioSteps) {
            	normalScenario.addBeforeSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta,
                        Stage.BEFORE, ScenarioType.NORMAL));
            }
            
			if (isParameterisedByExamples(scenario)) {
                ExamplesTable table = scenario.getExamplesTable();
                for (Map<String, String> scenarioParameters : table.getRows()) {
					ExamplePerformableScenario exampleScenario = exampleScenario(
							context, lifecycle, scenario, storyAndScenarioMeta,
							scenarioParameters);
					performableScenario.addExampleScenario(exampleScenario);
                }
            } else { // plain old scenario
				performableScenario.useNormalScenario(normalScenario);
            }

            // after scenario steps, if allowed
            if (runBeforeAndAfterScenarioSteps) {
            	normalScenario.addAfterSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.AFTER,
                        ScenarioType.NORMAL));
            }

        }
        return performableScenario;
    }

	private NormalPerformableScenario normalScenario(RunContext context,
			Lifecycle lifecycle, Scenario scenario, Meta storyAndScenarioMeta,
			Map<String, String> storyParameters) {
		NormalPerformableScenario normalScenario = new NormalPerformableScenario(scenario);
		addStepsWithLifecycle(normalScenario, context, lifecycle, storyParameters,
				scenario, storyAndScenarioMeta);
		return normalScenario;
	}

	private ExamplePerformableScenario exampleScenario(RunContext context,
			Lifecycle lifecycle, Scenario scenario, Meta storyAndScenarioMeta,
			Map<String, String> parameters) {
        ExamplePerformableScenario exampleScenario = new ExamplePerformableScenario(parameters);
        exampleScenario.addBeforeSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.BEFORE,
                ScenarioType.EXAMPLE));
        addStepsWithLifecycle(exampleScenario, context, lifecycle, parameters, scenario, storyAndScenarioMeta);
        exampleScenario.addAfterSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.AFTER,
                ScenarioType.EXAMPLE));
        return exampleScenario;
    }

	private void addStepsWithLifecycle(AbstractPerformableScenario performableScenario, RunContext context,
			Lifecycle lifecycle, Map<String, String> parameters, Scenario scenario, Meta storyAndScenarioMeta) {
		performableScenario.addBeforeSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.BEFORE,
                ScenarioType.ANY));
		performableScenario.addBeforeSteps(context.lifecycleSteps(lifecycle, storyAndScenarioMeta, Stage.BEFORE));
		addMetaParameters(parameters, storyAndScenarioMeta);
		performableScenario.addGivenStories(performableGivenStories(context, scenario.getGivenStories(),
		        parameters));
		performableScenario.addSteps(context.scenarioSteps(scenario, parameters));
		performableScenario.addAfterSteps(context.lifecycleSteps(lifecycle, storyAndScenarioMeta, Stage.AFTER));
		performableScenario.addAfterSteps(context.beforeOrAfterScenarioSteps(storyAndScenarioMeta, Stage.AFTER,
                ScenarioType.ANY));
	}

    private List<PerformableStory> performableGivenStories(RunContext context, GivenStories givenStories,
            Map<String, String> parameters) {
        List<PerformableStory> stories = new ArrayList<PerformableStory>();
        if (givenStories.getPaths().size() > 0) {
            for (GivenStory givenStory : givenStories.getStories()) {
                RunContext childContext = context.childContextFor(givenStory);
                // run given story, using any parameters provided
                Story story = storyOfPath(context.configuration(), childContext.path());                
                if ( givenStory.hasAnchorParameters() ){
                    story = storyWithMatchingScenarios(story, givenStory.getAnchorParameters());
                }
                parameters.putAll(givenStory.getParameters());
                stories.add(performableStory(childContext, story, parameters));
            }
        }
        return stories;
    }

    private Story storyWithMatchingScenarios(Story story, Map<String,String> parameters) {
        if ( parameters.isEmpty() ) return story;
        List<Scenario> scenarios = new ArrayList<Scenario>();
        for ( Scenario scenario : story.getScenarios() ){
            if ( matchesParameters(scenario, parameters) ){
                scenarios.add(scenario);
            }
        }
        return new Story(story.getPath(), story.getDescription(), story.getMeta(), story.getNarrative(), scenarios); 
    }

    private boolean matchesParameters(Scenario scenario, Map<String, String> parameters) {
        Meta meta = scenario.getMeta();
        for ( String name : parameters.keySet() ){
            if ( meta.hasProperty(name) ){
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
        
        State run(Step step, List<StepResult> results, StoryReporter reporter,
                UUIDExceptionWrapper storyFailureIfItHappened);

        UUIDExceptionWrapper getFailure();
    }

    private final static class FineSoFar implements State {

        public State run(Step step, List<StepResult> results, StoryReporter reporter,
                UUIDExceptionWrapper storyFailureIfItHappened) {
            if (step instanceof ParameterisedStep) {
                ((ParameterisedStep) step).describeTo(reporter);
            }
            StepResult result = step.perform(storyFailureIfItHappened);
            results.add(result);
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

        public UUIDExceptionWrapper getFailure() {
            return null;
        }

    }

    private final static class SomethingHappened implements State {
        private UUIDExceptionWrapper failure;

        public SomethingHappened(UUIDExceptionWrapper failure) {
            this.failure = failure;
        }

        public State run(Step step, List<StepResult> results, StoryReporter reporter, UUIDExceptionWrapper storyFailure) {
            StepResult result = step.doNotPerform(storyFailure);
            results.add(result);
            result.describeTo(reporter);
            return this;
        }

        public UUIDExceptionWrapper getFailure() {
            return failure;
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

    private void performCancellable(RunContext context, Story story) throws InterruptedException {
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
        private Map<String, List<PendingStep>> pendingStories = new HashMap<String, List<PendingStep>>();

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
            return new FilteredStory(filter, story, configuration.storyControls(), givenStory);
        }

        public MetaFilter filter() {
            return filter;
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

        public PerformableSteps lifecycleSteps(Lifecycle lifecycle, Meta meta, Stage stage) {
            MatchingStepMonitor monitor = new MatchingStepMonitor();
            List<Step> steps = configuration.stepCollector().collectLifecycleSteps(candidateSteps, lifecycle, meta, stage);
            return new PerformableSteps(steps, monitor.matched());
        }

        public PerformableSteps scenarioSteps(Scenario scenario, Map<String, String> parameters) {
            MatchingStepMonitor monitor = new MatchingStepMonitor();
            List<Step> steps = configuration.stepCollector().collectScenarioSteps(candidateSteps, scenario, parameters,
                    monitor);
            return new PerformableSteps(steps, monitor.matched());
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
                return state.getFailure().getCause();
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

        public void pendingSteps(List<PendingStep> pendingSteps) {
            if (!pendingSteps.isEmpty()) {
                pendingStories.put(path, pendingSteps);
            }
        }

        public boolean hasPendingSteps() {
            return pendingStories.containsKey(path);
        }

        public boolean isStoryPending() {
            return pendingStories.containsKey(path);
        }

        public boolean hasFailed() {
            return failed(state);
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
    }

    public static interface Performable {

        void perform(RunContext context) throws InterruptedException;

    }

    public static class PerformableRoot {

        private PerformableSteps beforeSteps = new PerformableSteps();
        private Map<String, PerformableStory> stories = new LinkedHashMap<String, PerformableStory>();
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
            return new ArrayList<PerformableStory>(stories.values());
        }

    }

    public static enum Status {
        SUCCESSFUL, FAILED, PENDING, NOT_PERFORMED, NOT_ALLOWED;
    }

    public static class PerformableStory implements Performable {

        private final Story story;
        private String localizedNarrative;
        private boolean allowed;
        private Status status;
        private List<PerformableStory> givenStories = new ArrayList<PerformableStory>();
        private List<PerformableScenario> scenarios = new ArrayList<PerformableScenario>();
        private PerformableSteps beforeSteps = new PerformableSteps();
        private PerformableSteps afterSteps = new PerformableSteps();
        private Timing timing = new Timing();
        private boolean givenStory;

        public PerformableStory(Story story, Keywords keywords, boolean givenStory) {
            this.story = story;
            this.givenStory = givenStory;
            this.localizedNarrative = story.getNarrative().asString(keywords);
        }

        public void allowed(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public boolean givenStory() {
            return givenStory;
        }

        public Status getStatus() {
            return status;
        }

        public void addGivenStories(List<PerformableStory> performableGivenStories) {
            this.givenStories.addAll(performableGivenStories);
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

        public Story getStory() {
            return story;
        }

        public String getLocalisedNarrative() {
            return localizedNarrative;
        }

        public Timing getTiming() {
            return timing;
        }

        public void perform(RunContext context) throws InterruptedException {
            if (!allowed) {
                context.reporter().storyNotAllowed(story, context.filter.asString());
                this.status = Status.NOT_ALLOWED;
            }
            context.reporter().beforeStory(story, context.givenStory);
            context.reporter().narrative(story.getNarrative());
            context.reporter().lifecyle(story.getLifecycle());
            State state = context.state();
            Timer timer = new Timer().start();
            try {
                performScenarios(context);
            } finally {
                timing.setDurationInMillis(timer.stop());
            }
            context.reporter().afterStory(context.givenStory);
            this.status = context.status(state);
        }

        private void performScenarios(RunContext context) throws InterruptedException {
            beforeSteps.perform(context);
            for (PerformableScenario scenario : scenarios) {
                scenario.perform(context);
            }
            afterSteps.perform(context);
        }

        public List<PerformableScenario> getScenarios() {
            return scenarios;
        }

    }

    public static class PerformableScenario implements Performable {

        private final Scenario scenario;
        private final String storyPath;
        private boolean allowed;
		private NormalPerformableScenario normalPerformableScenario;
        private List<ExamplePerformableScenario> examplePerformableScenarios = new ArrayList<ExamplePerformableScenario>();
        Status status;

        public PerformableScenario(Scenario scenario, String storyPath) {
            this.scenario = scenario;
            this.storyPath = storyPath;
        }

        public void useNormalScenario(NormalPerformableScenario normalScenario) {
            this.normalPerformableScenario = normalScenario;
        }

        public void addExampleScenario(ExamplePerformableScenario exampleScenario) {
            this.examplePerformableScenarios.add(exampleScenario);
        }

        public void allowed(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Scenario getScenario() {
            return scenario;
        }

        public String getStoryPath() {
            return storyPath;
        }

        public boolean hasExamples() {
            return examplePerformableScenarios.size() > 0;
        }

        public List<ExamplePerformableScenario> getExamples() {
            return examplePerformableScenarios;
        }

        public void perform(RunContext context) throws InterruptedException {
            context.reporter().beforeScenario(scenario.getTitle());
            State state = context.state();
			if (!examplePerformableScenarios.isEmpty()) {
				context.reporter().beforeExamples(scenario.getSteps(),
						scenario.getExamplesTable());
				for (ExamplePerformableScenario exampleScenario : examplePerformableScenarios) {
					exampleScenario.perform(context);
				}
				context.reporter().afterExamples();
			} else {
				normalPerformableScenario.perform(context);
			}
			this.status = context.status(state);
			context.reporter().afterScenario();
        }

    }

    public static abstract class AbstractPerformableScenario implements Performable {

        protected final Map<String, String> parameters;
        protected final List<PerformableStory> givenStories = new ArrayList<PerformableStory>();
        protected final PerformableSteps beforeSteps = new PerformableSteps();
        protected final PerformableSteps steps = new PerformableSteps();
        protected final PerformableSteps afterSteps = new PerformableSteps();

        public AbstractPerformableScenario() {
            this(new HashMap<String, String>());
        }

        public AbstractPerformableScenario(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        public void addGivenStories(List<PerformableStory> givenStories) {
            this.givenStories.addAll(givenStories);
        }

        public void addBeforeSteps(PerformableSteps beforeSteps) {
            this.beforeSteps.add(beforeSteps);
        }

        public void addSteps(PerformableSteps steps) {
            this.steps.add(steps);
        }

        public void addAfterSteps(PerformableSteps afterSteps) {
            this.afterSteps.add(afterSteps);
        }

        public Map<String, String> getParameters() {
            return parameters;
        }
    }

    public static class NormalPerformableScenario extends AbstractPerformableScenario {

        private Scenario scenario;

		public NormalPerformableScenario(Scenario scenario) {
			this.scenario = scenario;
        }

        public void perform(RunContext context) throws InterruptedException {
            if (context.configuration().storyControls().resetStateBeforeScenario()) {
                context.resetState();
            }
            beforeSteps.perform(context);
            if (scenario.getGivenStories().getPaths().size() > 0) {
                context.reporter().givenStories(scenario.getGivenStories());
                for (PerformableStory story : givenStories) {
                    context.givenStory = story.givenStory();
                    story.perform(context);
                }
            }
            steps.perform(context);
            afterSteps.perform(context);
        }

    }

    public static class ExamplePerformableScenario extends AbstractPerformableScenario {

        public ExamplePerformableScenario(Map<String, String> exampleParameters) {
        	super(exampleParameters);
        }

        public void perform(RunContext context) throws InterruptedException {
            if (!context.filter().allow(parameterMeta(context.configuration().keywords(), parameters))) {
                return;
            }
            if (context.configuration().storyControls().resetStateBeforeScenario()) {
                context.resetState();
            }
            context.reporter().example(parameters);
            beforeSteps.perform(context);
            for (PerformableStory story : givenStories) {
                story.perform(context);
            }
            steps.perform(context);
            afterSteps.perform(context);
        }

        private Meta parameterMeta(Keywords keywords, Map<String, String> parameters) {
            String meta = keywords.meta();
            if (parameters.containsKey(meta)) {
                return Meta.createMeta(parameters.get(meta), keywords);
            }
            return Meta.EMPTY;
        }

    }

    public static class PerformableSteps implements Performable {

        private transient final List<Step> steps;
        private transient final List<PendingStep> pendingSteps;
        private List<StepMatch> matches;
        private List<StepResult> results;

        public PerformableSteps() {
            this(null);
        }

        public PerformableSteps(List<Step> steps) {
            this(steps, null);
        }

        public PerformableSteps(List<Step> steps, List<StepMatch> stepMatches) {
            this.steps =  ( steps == null ? new ArrayList<Step>() : steps );
            this.pendingSteps = pendingSteps();
            this.matches = stepMatches;
        }

        public void add(PerformableSteps performableSteps){
            this.steps.addAll(performableSteps.steps);
            this.pendingSteps.addAll(performableSteps.pendingSteps);
            if ( performableSteps.matches != null ){
                if ( this.matches == null ){
                    this.matches = new ArrayList<StepMatch>();
                }
                this.matches.addAll(performableSteps.matches);
            }
        }
        
        public void perform(RunContext context) throws InterruptedException {
            if (steps.size() == 0) {
                return;
            }
            State state = context.state();
            StoryReporter reporter = context.reporter();
            results = new ArrayList<StepResult>();
            for (Step step : steps) {
                context.interruptIfCancelled();
                state = state.run(step, results, reporter, state.getFailure());
            }
            context.stateIs(state);
            context.pendingSteps(pendingSteps);
            generatePendingStepMethods(context, pendingSteps);
        }

        private List<PendingStep> pendingSteps() {
            List<PendingStep> pending = new ArrayList<PendingStep>();
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
                List<String> methods = new ArrayList<String>();
                for (PendingStep pendingStep : pendingSteps) {
                    if (!pendingStep.annotated()) {
                        methods.add(generator.generateMethod(pendingStep));
                    }
                }
                context.reporter().pendingMethods(methods);
                if ( context.configuration().pendingStepStrategy() instanceof FailingUponPendingStep ){
                     throw new PendingStepsFound(pendingSteps);
                }
            }
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }

    public static class Timing {
        private long durationInMillis;

        public long getDurationInMillis() {
            return durationInMillis;
        }

        public void setDurationInMillis(long durationInMillis) {
            this.durationInMillis = durationInMillis;
        }
    }

    public RunContext newRunContext(Configuration configuration, InjectableStepsFactory stepsFactory,
            MetaFilter filter, BatchFailures failures) {
        return new RunContext(configuration, stepsFactory, filter, failures);
    }

}
