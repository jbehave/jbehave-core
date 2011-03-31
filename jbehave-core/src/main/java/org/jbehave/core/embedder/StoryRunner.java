package org.jbehave.core.embedder;

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
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
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

    /**
     * Run steps before or after a collection of stories. Steps are execute only
     * <b>once</b> per collection of stories.
     * 
     * @param configuration the Configuration used to find the steps to run
     * @param candidateSteps the List of CandidateSteps containing the candidate
     *            steps methods
     * @param stage the Stage
     */
    public void runBeforeOrAfterStories(Configuration configuration, List<CandidateSteps> candidateSteps,
            Stage stage) {
        reporter.set(configuration.storyReporter(stage.name().toLowerCase()+"Stories"));
        runStepsWhileKeepingState(configuration.stepCollector().collectBeforeOrAfterStoriesSteps(candidateSteps, stage));
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
        RunContext context = new RunContext(configuration, filter, candidateSteps, story.getPath());
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
        if ( !context.givenStory ){
            reporter.set(reporterFor(context, story));
        }
        pendingStepStrategy.set(context.configuration().pendingStepStrategy());
        failureStrategy.set(context.configuration().failureStrategy());

        try {
            resetFailureState(context.givenStory());

            if (context.dryRun()) {
                reporter.get().dryRun();
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
                    if (failureOccurred() && context.configuration().storyControls().skipScenariosAfterFailure()) {
                        continue;
                    }
                    reporter.get().beforeScenario(scenario.getTitle());
                    reporter.get().scenarioMeta(scenario.getMeta());

                    if (context.metaNotAllowed(scenario.getMeta().inheritFrom(story.getMeta()))) {
                        reporter.get().scenarioNotAllowed(scenario, context.metaFilterAsString());
                        scenarioAllowed = false;
                    }

                    if (scenarioAllowed) {

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
            if (reporter.get() instanceof ConcurrentStoryReporter) {
                ((ConcurrentStoryReporter) reporter.get()).invokeDelayed();
            }
        }
    }

    private boolean shouldRunBeforeOrAfterScenarioSteps(RunContext context) {
        Configuration configuration = context.configuration();
        if (!configuration.storyControls().skipBeforeAndAfterScenarioStepsIfGivenStory()) {
            return true;
        }

        return !context.givenStory();
    }

    private boolean failureOccurred() {
        return storyFailure.get() != null;
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

    private void resetFailureState(boolean givenStory) {
        if (givenStory) {
            // do not reset failure state for given stories
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
        runStepsWhileKeepingState(context.collectBeforeOrAfterStorySteps(story, stage));
    }

    private void runBeforeOrAfterScenarioSteps(RunContext context, Scenario scenario, Stage stage) {
        runStepsWhileKeepingState(context.collectBeforeOrAfterScenarioSteps(stage));
    }

    private void runScenarioSteps(RunContext context, Scenario scenario, Map<String, String> scenarioParameters) {
        runStepsWhileKeepingState(context.collectScenarioSteps(scenario, scenarioParameters));
    }

    private void runStepsWhileKeepingState(List<Step> steps) {
        if (steps == null || steps.size() == 0) {
            return;
        }
        State state = new FineSoFar();
        for (Step step : steps) {
            state = state.run(step);
        }
    }

    private interface State {
        State run(Step step);
    }

    private final class FineSoFar implements State {

        public State run(Step step) {
            UUIDExceptionWrapper ifItHappened = storyFailure.get();
            StepResult result = step.perform(ifItHappened);
            result.describeTo(reporter.get());
            UUIDExceptionWrapper scenarioFailure = result.getFailure();
            if (scenarioFailure == null)
                return this;

            storyFailure.set(mostImportantOf(storyFailure.get(), scenarioFailure));
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

    private class SomethingHappened implements State {
        public State run(Step step) {
            StepResult result = step.doNotPerform();
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
        private final List<CandidateSteps> candidateSteps;
        private final MetaFilter filter;
        private final Configuration configuration;
        private final String path;
        private final boolean givenStory;

        private final StepCollector stepCollector;

        public RunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps, String path) {
            this(configuration, filter, steps, path, false);
        }

        private RunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps, String path,
                boolean givenStory) {
            this.configuration = configuration;
            this.filter = filter;
            this.candidateSteps = steps;
            this.path = path;
            this.givenStory = givenStory;

            this.stepCollector = configuration.stepCollector();
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

        public boolean metaNotAllowed(Meta meta) {
            return !filter.allow(meta);
        }

        public String metaFilterAsString() {
            return filter.asString();
        }

        public List<Step> collectBeforeOrAfterStorySteps(Story story, Stage stage) {
            return stepCollector.collectBeforeOrAfterStorySteps(candidateSteps, story, stage, givenStory);
        }

        public List<Step> collectBeforeOrAfterScenarioSteps(Stage stage) {
            return stepCollector.collectBeforeOrAfterScenarioSteps(candidateSteps, stage, storyFailure.get() != null);
        }

        public List<Step> collectScenarioSteps(Scenario scenario, Map<String, String> parameters) {
            return stepCollector.collectScenarioSteps(candidateSteps, scenario, parameters);
        }

        public RunContext childContextFor(GivenStory givenStory) {
            String actualPath = configuration.pathCalculator().calculate(path, givenStory.getPath());
            return new RunContext(configuration, filter, candidateSteps, actualPath, true);
        }
    }
}
