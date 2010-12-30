package org.jbehave.core.embedder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
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
    private FailureStrategy currentStrategy;
    private FailureStrategy failureStrategy;
    private PendingStepStrategy pendingStepStrategy;
    private Throwable storyFailure;
    private StepCollector stepCollector;
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
        StoryRunContext context = new StoryRunContext(configuration, MetaFilter.EMPTY, candidateSteps);
        run(story, new HashMap<String, String>(), context);
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
        StoryRunContext context = new StoryRunContext(configuration, filter, candidateSteps);
        run(story, new HashMap<String, String>(), context);
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

    private void run(Story story, Map<String, String> storyParameters, StoryRunContext context)
            throws Throwable {
        stepCollector = context.configuration().stepCollector();
        reporter = reporterFor(context, story);
        pendingStepStrategy = context.configuration().pendingStepStrategy();
        failureStrategy = context.configuration().failureStrategy();
        
        if (!context.metaFilter().allow(story.getMeta())) {
            reporter.storyNotAllowed(story, context.metaFilter().asString());
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
            if (!context.metaFilter().allow(scenario.getMeta().inheritFrom(story.getMeta()))) {
                reporter.scenarioNotAllowed(scenario, context.metaFilter().asString());
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

    private boolean shouldRunBeforeOrAfterScenarioSteps(StoryRunContext context) {
        Configuration configuration = context.configuration();
        if (!configuration.storyControls().skipBeforeAndAfterScenarioStepsIfGivenStory()){
            return true;
        }
        
        return !context.givenStory();
    }

    private boolean failureOccurred() {
        return storyFailure != null;
    }

    private StoryReporter reporterFor(StoryRunContext context, Story story) {
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

    private void runGivenStories(Scenario scenario, StoryRunContext context) throws Throwable {
        GivenStories givenStories = scenario.getGivenStories();
        if (givenStories.getPaths().size() > 0) {
            reporter.givenStories(givenStories);
            for (GivenStory givenStory : givenStories.getStories()) {
                // run given story, using any parameters if provided
                Story story = storyOfPath(context.configuration(), givenStory.getPath());
                run(story, givenStory.getParameters(), context.forGivenStory());
            }
        }
    }

    private boolean isParameterisedByExamples(Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() > 0 && !scenario.getGivenStories().requireParameters();
    }

    private void runParametrisedScenariosByExamples(StoryRunContext context, Scenario scenario) {
        ExamplesTable table = scenario.getExamplesTable();
        reporter.beforeExamples(scenario.getSteps(), table);
        for (Map<String, String> scenarioParameters : table.getRows()) {
            reporter.example(scenarioParameters);
            runScenarioSteps(context, scenario, scenarioParameters);
        }
        reporter.afterExamples();
    }

    private void runBeforeOrAfterStorySteps(StoryRunContext context, Story story, Stage stage) {
        List<CandidateSteps> candidateSteps = context.candidateSteps();
        boolean givenStory = context.givenStory();
        runStepsWhileKeepingState(stepCollector.collectBeforeOrAfterStorySteps(candidateSteps, story, stage, givenStory));
    }

    private void runBeforeOrAfterScenarioSteps(StoryRunContext context, Scenario scenario, Stage stage) {
        List<CandidateSteps> candidateSteps = context.candidateSteps();
        runStepsWhileKeepingState(stepCollector.collectBeforeOrAfterScenarioSteps(candidateSteps, stage));
    }

    private void runScenarioSteps(StoryRunContext context, Scenario scenario, Map<String, String> scenarioParameters) {
        List<CandidateSteps> candidateSteps = context.candidateSteps();
        runStepsWhileKeepingState(stepCollector.collectScenarioSteps(candidateSteps, scenario, scenarioParameters));
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
            StepResult result = step.perform();
            result.describeTo(reporter);
            Throwable scenarioFailure = result.getFailure();
            if (scenarioFailure == null)
                return this;

            storyFailure = mostImportantOf(storyFailure, scenarioFailure);
            currentStrategy = strategyFor(storyFailure);
            return new SomethingHappened();
        }

        private Throwable mostImportantOf(Throwable failure1, Throwable failure2) {
            return failure1 == null ? failure2 : failure1 instanceof PendingStepFound ? (failure2 == null ? failure1
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

}
