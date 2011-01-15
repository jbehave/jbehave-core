package org.jbehave.core.embedder;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.*;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.*;
import org.jbehave.core.steps.StepCollector.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a {@link Story}, given a {@link Configuration} and a list of
 * {@link CandidateSteps}, describing the results to the {@link StoryReporter}.
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 * @author Paul Hammant
 */
public class StoryRunner {
    private FailureStrategy currentStrategy;
    private FailureStrategy failureStrategy;
    private PendingStepStrategy pendingStepStrategy;
    private UUIDExceptionWrapper storyFailure;
    private StoryReporter reporter;
    private String reporterStoryPath;

    /**
     * Run steps before or after a collection of stories. Steps are execute only
     * <b>once</b> per collection of stories.
     * 
     * @param configuration the Configuration used to find the steps to run
     * @param candidateSteps List of CandidateSteps containing the candidate
     *            steps methods
     * @param stage the Stage
     */
    public void runBeforeOrAfterStories(Configuration configuration, List<CandidateSteps> candidateSteps, Stage stage) {
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
    public void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story)
            throws Throwable {
        RunContext context = new RunContext(configuration, MetaFilter.EMPTY, candidateSteps, story.getPath());
        run(context, story, new HashMap<String, String>());
    }

    /**
     * Runs a Story with the given configuration and steps, applying the given meta filter.
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
        run(context, story, new HashMap<String, String>());
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

    private void run(RunContext context, Story story, Map<String, String> storyParameters)
            throws Throwable {
        reporter = reporterFor(context, story);
        pendingStepStrategy = context.configuration().pendingStepStrategy();
        failureStrategy = context.configuration().failureStrategy();
        
        if (context.metaNotAllowed(story.getMeta())) {
            reporter.storyNotAllowed(story, context.metaFilterAsString());
            return;
        }

        resetFailureState(context.givenStory());

        if (context.dryRun()) {
            reporter.dryRun();
        }

        // run before story steps, if any
        reporter.beforeStory(story, context.givenStory());
        runBeforeOrAfterStorySteps(context, story, StepCollector.Stage.BEFORE);
        
        // determine if before and after scenario steps should be run
        boolean runBeforeAndAfterScenarioSteps = shouldRunBeforeOrAfterScenarioSteps(context);
        
        for (Scenario scenario : story.getScenarios()) {
            // scenario also inherits meta from story
            if (context.metaNotAllowed(scenario.getMeta().inheritFrom(story.getMeta()))) {
                reporter.scenarioNotAllowed(scenario, context.metaFilterAsString());
                continue;
            }
            if (failureOccurred() && context.configuration().storyControls().skipScenariosAfterFailure()) {
                continue;
            }
            reporter.beforeScenario(scenario.getTitle());
            reporter.scenarioMeta(scenario.getMeta());

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

            reporter.afterScenario();
        }

        // run after story steps, if any
        runBeforeOrAfterStorySteps(context, story, StepCollector.Stage.AFTER);
        reporter.afterStory(context.givenStory());

        // handle any failure according to strategy
        if (!context.givenStory()) {
            currentStrategy.handleFailure(storyFailure);
        }
    }

    private boolean shouldRunBeforeOrAfterScenarioSteps(RunContext context) {
        Configuration configuration = context.configuration();
        if (!configuration.storyControls().skipBeforeAndAfterScenarioStepsIfGivenStory()){
            return true;
        }
        
        return !context.givenStory();
    }

    private boolean failureOccurred() {
        return storyFailure != null;
    }

    private StoryReporter reporterFor(RunContext context, Story story) {
        Configuration configuration = context.configuration();
        if (context.givenStory()) {
            return configuration.storyReporter(reporterStoryPath);
        } else {
            // store parent story path for reporting
            reporterStoryPath = story.getPath();
            return configuration.storyReporter(reporterStoryPath);
        }
    }

    private void resetFailureState(boolean givenStory) {
        if (givenStory) {
            // do not reset failure state for given stories
            return;
        }
        currentStrategy = new SilentlyAbsorbingFailure();
        storyFailure = null;
    }

    private void runGivenStories(Scenario scenario, RunContext context) throws Throwable {
        GivenStories givenStories = scenario.getGivenStories();
        if (givenStories.getPaths().size() > 0) {
            reporter.givenStories(givenStories);
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
        reporter.beforeExamples(scenario.getSteps(), table);
        for (Map<String, String> scenarioParameters : table.getRows()) {
            reporter.example(scenarioParameters);
            runScenarioSteps(context, scenario, scenarioParameters);
        }
        reporter.afterExamples();
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
        if (steps == null || steps.size() == 0){
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
            StepResult result = step.perform(storyFailure);
            result.describeTo(reporter);
            UUIDExceptionWrapper scenarioFailure = result.getFailure();
            if (scenarioFailure == null)
                return this;

            storyFailure = mostImportantOf(storyFailure, scenarioFailure);
            currentStrategy = strategyFor(storyFailure);
            return new SomethingHappened();
        }

        private UUIDExceptionWrapper mostImportantOf(UUIDExceptionWrapper failure1, UUIDExceptionWrapper failure2) {
            return failure1 == null ? failure2 : failure1.getCause() instanceof PendingStepFound ? (failure2 == null ? failure1
                    : failure2) : failure1;
        }

        private FailureStrategy strategyFor(Throwable failure) {
            if (failure instanceof PendingStepFound) {
                return pendingStepStrategy;
            } else {
                return failureStrategy;
            }
        }
    }

    private class SomethingHappened implements State {
        public State run(Step step) {
            StepResult result = step.doNotPerform();
            result.describeTo(reporter);
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

        private RunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps,
                String path, boolean givenStory) {
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
            return stepCollector.collectBeforeOrAfterScenarioSteps(candidateSteps, stage, storyFailure !=null);
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
