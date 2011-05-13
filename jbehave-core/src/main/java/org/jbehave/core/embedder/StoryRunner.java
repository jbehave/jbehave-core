package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.PendingStepMethodGenerator;
import org.jbehave.core.steps.ProvidedStepsFactory;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepResult;

import static org.codehaus.plexus.util.StringUtils.capitalizeFirstLetter;

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
        if (stage == Stage.AFTER && storiesState.get() != null) {
            context.stateIs(storiesState.get());
        }
        runStepsWhileKeepingState(context,
                configuration.stepCollector().collectBeforeOrAfterStoriesSteps(context.candidateSteps(), stage));
        reporter.get().afterStory(false);
        storiesState.set(context.state());
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

    private void run(RunContext context, Story story, Map<String, String> storyParameters) throws Throwable {
        if (!context.givenStory) {
            reporter.set(reporterFor(context, story));
        }
        pendingStepStrategy.set(context.configuration().pendingStepStrategy());
        failureStrategy.set(context.configuration().failureStrategy());

        try {
            resetStoryFailure(context.givenStory());

            if (context.dryRun()) {
                reporter.get().dryRun();
            }

            if (context.configuration().storyControls().resetStateBeforeStory()) {
                context.resetState();
            }

            // run before story steps, if any
            reporter.get().beforeStory(story, context.givenStory());

            boolean storyAllowed = true;

            if (context.metaNotAllowed(story.getMeta())) {
                reporter.get().storyNotAllowed(story, context.metaFilterAsString());
                storyAllowed = false;
            }

            if (storyAllowed) {

                reporter.get().narrative(story.getNarrative());

                runBeforeOrAfterStorySteps(context, story, Stage.BEFORE);

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

                    if (context.metaNotAllowed(scenario.getMeta().inheritFrom(story.getMeta()))) {
                        reporter.get().scenarioNotAllowed(scenario, context.metaFilterAsString());
                        scenarioAllowed = false;
                    }

                    if (scenarioAllowed) {
                        if (context.configuration().storyControls().resetStateBeforeScenario()) {
                            context.resetState();
                        }
                        // run before scenario steps, if allowed
                        if (runBeforeAndAfterScenarioSteps) {
                            runBeforeOrAfterScenarioSteps(context, scenario, Stage.BEFORE);
                        }

                        // run given stories, if any
                        runGivenStories(scenario, context);
                        if (isParameterisedByExamples(scenario)) {
                            // run parametrised scenarios by examples
                            runParametrisedScenariosByExamples(context, scenario);
                        } else { // run as plain old scenario
                            addMetaParameters(storyParameters, scenario.getMeta().inheritFrom(story.getMeta()));
                            runScenarioSteps(context, scenario, storyParameters);
                        }

                        // run after scenario steps, if allowed
                        if (runBeforeAndAfterScenarioSteps) {
                            runBeforeOrAfterScenarioSteps(context, scenario, Stage.AFTER);
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
                currentStrategy.get().handleFailure(storyFailure.get());
            }
        } finally {
            if (!context.givenStory() && reporter.get() instanceof ConcurrentStoryReporter) {
                ((ConcurrentStoryReporter) reporter.get()).invokeDelayed();
            }
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

    private void resetStoryFailure(boolean givenStory) {
        if (givenStory) {
            // do not reset failure for given stories
            return;
        }
        currentStrategy.set(new SilentlyAbsorbingFailure());
        storyFailure.set(null);
    }

    private void runGivenStories(Scenario scenario, RunContext context) throws Throwable {
        GivenStories givenStories = scenario.getGivenStories();
        if (givenStories.getPaths().size() > 0) {
            reporter.get().givenStories(givenStories);
            for (GivenStory givenStory : givenStories.getStories()) {
                RunContext childContext = context.childContextFor(givenStory);
                // run given story, using any parameters if provided
                Story story = storyOfPath(context.configuration(), childContext.path());
                run(childContext, story, givenStory.getParameters());
            }
        }
    }

    private boolean isParameterisedByExamples(Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() > 0 && !scenario.getGivenStories().requireParameters();
    }

    private void runParametrisedScenariosByExamples(RunContext context, Scenario scenario) {
        ExamplesTable table = scenario.getExamplesTable();
        reporter.get().beforeExamples(scenario.getSteps(), table);
        for (Map<String, String> scenarioParameters : table.getRows()) {
            reporter.get().example(scenarioParameters);
            runScenarioSteps(context, scenario, scenarioParameters);
        }
        reporter.get().afterExamples();
    }

    private void runBeforeOrAfterStorySteps(RunContext context, Story story, Stage stage) {
        runStepsWhileKeepingState(context, context.collectBeforeOrAfterStorySteps(story, stage));
    }

    private void runBeforeOrAfterScenarioSteps(RunContext context, Scenario scenario, Stage stage) {
        runStepsWhileKeepingState(context, context.collectBeforeOrAfterScenarioSteps(stage));
    }

    private void runScenarioSteps(RunContext context, Scenario scenario, Map<String, String> scenarioParameters) {
        List<Step> steps = context.collectScenarioSteps(scenario, scenarioParameters);
        runStepsWhileKeepingState(context, steps);
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
            reporter.get().pendingMethods(methods);
        }
    }

    private void runStepsWhileKeepingState(RunContext context, List<Step> steps) {
        if (steps == null || steps.size() == 0) {
            return;
        }
        State state = context.state();
        for (Step step : steps) {
            state = state.run(step);
        }
        context.stateIs(state);
    }

    interface State {
        State run(Step step);
    }

    private final class FineSoFar implements State {

        public State run(Step step) {
            UUIDExceptionWrapper storyFailureIfItHappened = storyFailure.get();
            StepResult result = step.perform(storyFailureIfItHappened);
            result.describeTo(reporter.get());
            UUIDExceptionWrapper stepFailure = result.getFailure();
            if (stepFailure == null) {
                return this;
            }

            storyFailure.set(mostImportantOf(storyFailureIfItHappened, stepFailure));
            currentStrategy.set(strategyFor(storyFailure.get()));
            return new SomethingHappened();
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
        public State run(Step step) {
            UUIDExceptionWrapper storyFailureIfItHappened = storyFailure.get();
            StepResult result = step.doNotPerform(storyFailureIfItHappened);
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

        public RunContext(Configuration configuration, InjectableStepsFactory stepsFactory, String path,
                MetaFilter filter) {
            this(configuration, stepsFactory.createCandidateSteps(), path, filter);
        }

        public RunContext(Configuration configuration, List<CandidateSteps> steps, String path, MetaFilter filter) {
            this(configuration, steps, path, filter, false);
        }

        private RunContext(Configuration configuration, List<CandidateSteps> steps, String path, MetaFilter filter,
                boolean givenStory) {
            this.configuration = configuration;
            this.candidateSteps = steps;
            this.path = path;
            this.filter = filter;
            this.givenStory = givenStory;
            resetState();
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

        public boolean metaNotAllowed(Meta meta) {
            return !filter.allow(meta);
        }

        public String metaFilterAsString() {
            return filter.asString();
        }

        public List<Step> collectBeforeOrAfterStorySteps(Story story, Stage stage) {
            return configuration.stepCollector().collectBeforeOrAfterStorySteps(candidateSteps, story, stage,
                    givenStory);
        }

        public List<Step> collectBeforeOrAfterScenarioSteps(Stage stage) {
            return configuration.stepCollector().collectBeforeOrAfterScenarioSteps(candidateSteps, stage,
                    failureOccurred());
        }

        public List<Step> collectScenarioSteps(Scenario scenario, Map<String, String> parameters) {
            return configuration.stepCollector().collectScenarioSteps(candidateSteps, scenario, parameters);
        }

        public RunContext childContextFor(GivenStory givenStory) {
            String actualPath = configuration.pathCalculator().calculate(path, givenStory.getPath());
            return new RunContext(configuration, candidateSteps, actualPath, filter, true);
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

    }

    public boolean failed(State state) {
        return !state.getClass().equals(FineSoFar.class);
    }
}
