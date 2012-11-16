package org.jbehave.core.embedder;

import static org.codehaus.plexus.util.StringUtils.capitalizeFirstLetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RestartingScenarioFailure;
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
import org.jbehave.core.steps.ProvidedStepsFactory;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.ParameterisedStep;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepResult;

/**
 * Runs a {@link Story}, given a {@link Configuration} and a list of
 * {@link CandidateSteps}, describing the results to the {@link StoryReporter}.
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 * @author Paul Hammant
 */
public class StoryRunner {

    private ThreadLocal<FailureStrategy> currentStrategy = new ThreadLocal<FailureStrategy>();
    private ThreadLocal<FailureStrategy> failureStrategy = new ThreadLocal<FailureStrategy>();
    private ThreadLocal<PendingStepStrategy> pendingStepStrategy = new ThreadLocal<PendingStepStrategy>();
    private ThreadLocal<UUIDExceptionWrapper> storyFailure = new ThreadLocal<UUIDExceptionWrapper>();
    private ThreadLocal<StoryReporter> reporter = new ThreadLocal<StoryReporter>();
    private ThreadLocal<String> reporterStoryPath = new ThreadLocal<String>();
    private ThreadLocal<State> storiesState = new ThreadLocal<State>();
    // should this be volatile?
    private Map<Story, StoryDuration> cancelledStories = new HashMap<Story, StoryDuration>();

    /**
     * Run steps before or after a collection of stories. Steps are execute only
     * <b>once</b> per collection of stories.
     * 
     * @param configuration the Configuration used to find the steps to run
     * @param candidateSteps the List of CandidateSteps containing the candidate
     *            steps methods
     * @param stage the Stage
     * @return The State after running the steps
     */
    public State runBeforeOrAfterStories(Configuration configuration, List<CandidateSteps> candidateSteps, Stage stage) {
        String storyPath = capitalizeFirstLetter(stage.name().toLowerCase()) + "Stories";
        reporter.set(configuration.storyReporter(storyPath));
        reporter.get().beforeStory(new Story(storyPath), false);
        RunContext context = new RunContext(configuration, candidateSteps, storyPath, MetaFilter.EMPTY);
        if (stage == Stage.BEFORE) {
            resetStoryFailure(context);
        }
        if (stage == Stage.AFTER && storiesState.get() != null) {
            context.stateIs(storiesState.get());
        }
        try {
            runStepsWhileKeepingState(context,
                    configuration.stepCollector().collectBeforeOrAfterStoriesSteps(context.candidateSteps(), stage));
        } catch (InterruptedException e) {
            throw new UUIDExceptionWrapper(e);
        }
        reporter.get().afterStory(false);
        storiesState.set(context.state());
        // if we are running with multiple threads, call delayed
        // methods, otherwise we will forget to close files on BeforeStories
        if (stage == Stage.BEFORE) {
            if (reporter.get() instanceof ConcurrentStoryReporter) {
                ((ConcurrentStoryReporter) reporter.get()).invokeDelayed();
            }
        }
        // handle any after stories failure according to strategy
        if (stage == Stage.AFTER) {
            try {
                handleStoryFailureByStrategy();
            } catch (Throwable e) {
                return new SomethingHappened(storyFailure.get());
            } finally {
                if (reporter.get() instanceof ConcurrentStoryReporter) {
                    ((ConcurrentStoryReporter) reporter.get()).invokeDelayed();
                }
            }
        }
        return context.state();
    }

    /**
     * Runs a Story with the given configuration and steps.
     * 
     * @param configuration the Configuration used to run story
     * @param candidateSteps the List of CandidateSteps containing the candidate
     *            steps methods
     * @param story the Story to run
     * @throws Throwable if failures occurred and FailureStrategy dictates it to
     *             be re-thrown.
     */
    public void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story) throws Throwable {
        run(configuration, candidateSteps, story, MetaFilter.EMPTY);
    }

    /**
     * Runs a Story with the given configuration and steps, applying the given
     * meta filter.
     * 
     * @param configuration the Configuration used to run story
     * @param candidateSteps the List of CandidateSteps containing the candidate
     *            steps methods
     * @param story the Story to run
     * @param filter the Filter to apply to the story Meta
     * @throws Throwable if failures occurred and FailureStrategy dictates it to
     *             be re-thrown.
     */
    public void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story, MetaFilter filter)
            throws Throwable {
        run(configuration, candidateSteps, story, filter, null);
    }

    /**
     * Runs a Story with the given configuration and steps, applying the given
     * meta filter, and staring from given state.
     * 
     * @param configuration the Configuration used to run story
     * @param candidateSteps the List of CandidateSteps containing the candidate
     *            steps methods
     * @param story the Story to run
     * @param filter the Filter to apply to the story Meta
     * @param beforeStories the State before running any of the stories, if not
     *            <code>null</code>
     * @throws Throwable if failures occurred and FailureStrategy dictates it to
     *             be re-thrown.
     */
    public void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story, MetaFilter filter,
            State beforeStories) throws Throwable {
        run(configuration, new ProvidedStepsFactory(candidateSteps), story, filter, beforeStories);
    }

    /**
     * Runs a Story with the given steps factory, applying the given meta
     * filter, and staring from given state.
     * 
     * @param configuration the Configuration used to run story
     * @param stepsFactory the InjectableStepsFactory used to created the
     *            candidate steps methods
     * @param story the Story to run
     * @param filter the Filter to apply to the story Meta
     * @param beforeStories the State before running any of the stories, if not
     *            <code>null</code>
     * 
     * @throws Throwable if failures occurred and FailureStrategy dictates it to
     *             be re-thrown.
     */
    public void run(Configuration configuration, InjectableStepsFactory stepsFactory, Story story, MetaFilter filter,
            State beforeStories) throws Throwable {
        RunContext context = new RunContext(configuration, stepsFactory, story.getPath(), filter);
        if (beforeStories != null) {
            context.stateIs(beforeStories);
        }
        Map<String, String> storyParameters = new HashMap<String, String>();
        run(context, story, storyParameters);
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

    /**
     * Cancels story execution following a timeout
     * 
     * @param story the Story that was timed out
     * @param storyDuration the StoryDuration
     */
    public void cancelStory(Story story, StoryDuration storyDuration) {
        cancelledStories.put(story, storyDuration);
    }

    private void run(RunContext context, Story story, Map<String, String> storyParameters) throws Throwable {
        try {
            runCancellable(context, story, storyParameters);
        } catch (Throwable e) {
            if (cancelledStories.containsKey(story)) {
                reporter.get().storyCancelled(story, cancelledStories.get(story));
                reporter.get().afterStory(context.givenStory);
            }
            throw e;
        } finally {
            if (!context.givenStory() && reporter.get() instanceof ConcurrentStoryReporter) {
                ((ConcurrentStoryReporter) reporter.get()).invokeDelayed();
            }
        }
    }

    private void runCancellable(RunContext context, Story story, Map<String, String> storyParameters) throws Throwable {
        if (!context.givenStory()) {
            reporter.set(reporterFor(context, story));
        }
        pendingStepStrategy.set(context.configuration().pendingStepStrategy());
        failureStrategy.set(context.configuration().failureStrategy());

        resetStoryFailure(context);

        if (context.dryRun()) {
            reporter.get().dryRun();
        }

        if (context.configuration().storyControls().resetStateBeforeStory()) {
            context.resetState();
        }

        // run before story steps, if any
        reporter.get().beforeStory(story, context.givenStory());

        boolean storyAllowed = true;

        FilteredStory filterContext = context.filter(story);
        Meta storyMeta = story.getMeta();
        if (!filterContext.allowed()) {
            reporter.get().storyNotAllowed(story, context.metaFilterAsString());
            storyAllowed = false;
        }

        if (storyAllowed) {

            reporter.get().narrative(story.getNarrative());

            runBeforeOrAfterStorySteps(context, story, Stage.BEFORE);

            addMetaParameters(storyParameters, storyMeta);

            runGivenStories(story.getGivenStories(), storyParameters, context);
            
            // determine if before and after scenario steps should be run
            boolean runBeforeAndAfterScenarioSteps = shouldRunBeforeOrAfterScenarioSteps(context);

            for (Scenario scenario : story.getScenarios()) {
                // scenario also inherits meta from story
                boolean scenarioAllowed = true;
                if (failureOccurred(context) && context.configuration().storyControls().skipScenariosAfterFailure()) {
                    continue;
                }
                reporter.get().beforeScenario(scenario.getTitle());
                reporter.get().scenarioMeta(scenario.getMeta());

                if (!filterContext.allowed(scenario)) {
                    reporter.get().scenarioNotAllowed(scenario, context.metaFilterAsString());
                    scenarioAllowed = false;
                }

                if (scenarioAllowed) {
                    if (context.configuration().storyControls().resetStateBeforeScenario()) {
                        context.resetState();
                    }
                    Meta storyAndScenarioMeta = scenario.getMeta().inheritFrom(storyMeta);
                    // run before scenario steps, if allowed
                    if (runBeforeAndAfterScenarioSteps) {
                        runBeforeOrAfterScenarioSteps(context, scenario, storyAndScenarioMeta, Stage.BEFORE,
                                ScenarioType.NORMAL);
                    }

                    if (isParameterisedByExamples(scenario)) { // run parametrised scenarios by examples
                        runScenariosParametrisedByExamples(context, scenario, storyAndScenarioMeta);
                    } else { // run as plain old scenario
                        addMetaParameters(storyParameters, storyAndScenarioMeta);
                        runGivenStories(scenario.getGivenStories(), storyParameters, context);
                        runScenarioSteps(context, scenario, storyParameters);
                    }

                    // run after scenario steps, if allowed
                    if (runBeforeAndAfterScenarioSteps) {
                        runBeforeOrAfterScenarioSteps(context, scenario, storyAndScenarioMeta, Stage.AFTER,
                                ScenarioType.NORMAL);
                    }

                }

                reporter.get().afterScenario();
            }

            // run after story steps, if any
            runBeforeOrAfterStorySteps(context, story, Stage.AFTER);

        }

        reporter.get().afterStory(context.givenStory());

        // handle any failure according to strategy
        if (!context.givenStory()) {
            handleStoryFailureByStrategy();
        }
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

    private StoryReporter reporterFor(RunContext context, Story story) {
        Configuration configuration = context.configuration();
        if (context.givenStory()) {
            return configuration.storyReporter(reporterStoryPath.get());
        } else {
            // store parent story path for reporting
            reporterStoryPath.set(story.getPath());
            return configuration.storyReporter(reporterStoryPath.get());
        }
    }

    private void handleStoryFailureByStrategy() throws Throwable {
        Throwable throwable = storyFailure.get();
        if (throwable != null) {
            currentStrategy.get().handleFailure(throwable);
        }
    }

    private void resetStoryFailure(RunContext context) {
        if (context.givenStory()) {
            // do not reset failure for given stories
            return;
        }
        currentStrategy.set(context.configuration().failureStrategy());
        storyFailure.set(null);
    }

    private void runGivenStories(GivenStories givenStories, Map<String, String> parameters, RunContext context) throws Throwable {
        if (givenStories.getPaths().size() > 0) {
            reporter.get().givenStories(givenStories);
            for (GivenStory givenStory : givenStories.getStories()) {
                RunContext childContext = context.childContextFor(givenStory);
                // run given story, using any parameters provided
                Story story = storyOfPath(context.configuration(), childContext.path());
                parameters.putAll(givenStory.getParameters());
                run(childContext, story, parameters);
            }
        }
    }

    private boolean isParameterisedByExamples(Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() > 0 && !scenario.getGivenStories().requireParameters();
    }

    private void runScenariosParametrisedByExamples(RunContext context, Scenario scenario, Meta storyAndScenarioMeta)
            throws Throwable {
        ExamplesTable table = scenario.getExamplesTable();
        reporter.get().beforeExamples(scenario.getSteps(), table);
    	Keywords keywords = context.configuration().keywords();
        for (Map<String, String> scenarioParameters : table.getRows()) {
			Meta parameterMeta = parameterMeta(keywords, scenarioParameters);
			if ( !context.filter.allow(parameterMeta) ){
				continue;
			}
            reporter.get().example(scenarioParameters);
            if (context.configuration().storyControls().resetStateBeforeScenario()) {
                context.resetState();
            }
            runBeforeOrAfterScenarioSteps(context, scenario, storyAndScenarioMeta, Stage.BEFORE, ScenarioType.EXAMPLE);
            addMetaParameters(scenarioParameters, storyAndScenarioMeta);
            runGivenStories(scenario.getGivenStories(), scenarioParameters, context);
            runScenarioSteps(context, scenario, scenarioParameters);
            runBeforeOrAfterScenarioSteps(context, scenario, storyAndScenarioMeta, Stage.AFTER, ScenarioType.EXAMPLE);
        }
        reporter.get().afterExamples();
    }

	private Meta parameterMeta(Keywords keywords,
			Map<String, String> scenarioParameters) {
		String meta = keywords.meta();
		if (scenarioParameters.containsKey(meta)) {
			return Meta.createMeta(scenarioParameters.get(meta), keywords);
		}
		return Meta.EMPTY;
	}

    private void runBeforeOrAfterStorySteps(RunContext context, Story story, Stage stage) throws InterruptedException {
        runStepsWhileKeepingState(context, context.collectBeforeOrAfterStorySteps(story, stage));
    }

    private void runBeforeOrAfterScenarioSteps(RunContext context, Scenario scenario, Meta storyAndScenarioMeta,
            Stage stage, ScenarioType type) throws InterruptedException {
        runStepsWhileKeepingState(context, context.collectBeforeOrAfterScenarioSteps(storyAndScenarioMeta, stage, type));
    }

    private void runScenarioSteps(RunContext context, Scenario scenario, Map<String, String> scenarioParameters)
            throws InterruptedException {
        boolean restart = true;
        while (restart) {
            restart = false;
            List<Step> steps = context.collectScenarioSteps(scenario, scenarioParameters);
            try {
                runStepsWhileKeepingState(context, steps);
            } catch (RestartingScenarioFailure e) {
                restart = true;
                continue;
            }
            generatePendingStepMethods(context, steps);
        }
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
            reporter.get().pendingMethods(methods);
        }
    }

    private void runStepsWhileKeepingState(RunContext context, List<Step> steps) throws InterruptedException {
        if (steps == null || steps.size() == 0) {
            return;
        }
        State state = context.state();
        for (Step step : steps) {
            try {
                context.interruptIfCancelled();
                state = state.run(step);
            } catch (RestartingScenarioFailure e) {
                reporter.get().restarted(step.toString(), e);
                throw e;
            }
        }
        context.stateIs(state);
    }

    public interface State {
        State run(Step step);
    }

    private final class FineSoFar implements State {

        public State run(Step step) {
            if ( step instanceof ParameterisedStep ){
                ((ParameterisedStep)step).describeTo(reporter.get());
            }
            UUIDExceptionWrapper storyFailureIfItHappened = storyFailure.get(); 
            StepResult result = step.perform(storyFailureIfItHappened);
            result.describeTo(reporter.get());
            UUIDExceptionWrapper stepFailure = result.getFailure();
            if (stepFailure == null) {
                return this;
            }

            storyFailure.set(mostImportantOf(storyFailureIfItHappened, stepFailure));
            currentStrategy.set(strategyFor(storyFailure.get()));
            return new SomethingHappened(stepFailure);
        }

        private UUIDExceptionWrapper mostImportantOf(UUIDExceptionWrapper failure1, UUIDExceptionWrapper failure2) {
            return failure1 == null ? failure2
                    : failure1.getCause() instanceof PendingStepFound ? (failure2 == null ? failure1 : failure2)
                            : failure1;
        }

        private FailureStrategy strategyFor(Throwable failure) {
            if (failure instanceof PendingStepFound) {
                return pendingStepStrategy.get();
            } else {
                return failureStrategy.get();
            }
        }
    }

    private final class SomethingHappened implements State {
        UUIDExceptionWrapper scenarioFailure;

        public SomethingHappened(UUIDExceptionWrapper scenarioFailure) {
            this.scenarioFailure = scenarioFailure;
        }

        public State run(Step step) {
            StepResult result = step.doNotPerform(scenarioFailure);
            result.describeTo(reporter.get());
            return this;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * The context for running a story.
     */
    private class RunContext {
        private final Configuration configuration;
        private final List<CandidateSteps> candidateSteps;
        private final String path;
        private final MetaFilter filter;
        private final boolean givenStory;
		private State state;
		private RunContext parentContext;

        public RunContext(Configuration configuration, InjectableStepsFactory stepsFactory, String path,
                MetaFilter filter) {
            this(configuration, stepsFactory.createCandidateSteps(), path, filter);
        }

        public RunContext(Configuration configuration, List<CandidateSteps> steps, String path, MetaFilter filter) {
            this(configuration, steps, path, filter, false, null);
        }

        private RunContext(Configuration configuration, List<CandidateSteps> steps, String path, MetaFilter filter,
                boolean givenStory, RunContext parentContext) {
            this.configuration = configuration;
            this.candidateSteps = steps;
            this.path = path;
            this.filter = filter;
            this.givenStory = givenStory;
			this.parentContext = parentContext;
            resetState();
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

        public List<CandidateSteps> candidateSteps() {
            return candidateSteps;
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

        public List<Step> collectBeforeOrAfterStorySteps(Story story, Stage stage) {
            return configuration.stepCollector().collectBeforeOrAfterStorySteps(candidateSteps, story, stage,
                    givenStory);
        }

        public List<Step> collectBeforeOrAfterScenarioSteps(Meta storyAndScenarioMeta, Stage stage, ScenarioType type) {
            return configuration.stepCollector().collectBeforeOrAfterScenarioSteps(candidateSteps,
                    storyAndScenarioMeta, stage, type);
        }

        public List<Step> collectScenarioSteps(Scenario scenario, Map<String, String> parameters) {
            return configuration.stepCollector().collectScenarioSteps(candidateSteps, scenario, parameters);
        }

        public RunContext childContextFor(GivenStory givenStory) {
            String actualPath = configuration.pathCalculator().calculate(path, givenStory.getPath());
            return new RunContext(configuration, candidateSteps, actualPath, filter, true, this);
        }

        public State state() {
            return state;
        }

        public void stateIs(State state) {
            this.state = state;
            if ( parentContext != null ){
            	parentContext.stateIs(state);
            }
        }

        public boolean failureOccurred() {
            return failed(state);
        }

        public void resetState() {
            this.state = new FineSoFar();
        }

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
}
