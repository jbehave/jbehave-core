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

    private State state = new FineSoFar();
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
        run(configuration, candidateSteps, story, MetaFilter.EMPTY, false, new HashMap<String, String>());
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
        run(configuration, candidateSteps, story, filter, false, new HashMap<String, String>());
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

    private void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story, MetaFilter filter,
            boolean givenStory, Map<String, String> storyParameters) throws Throwable {
        stepCollector = configuration.stepCollector();
        reporter = reporterFor(configuration, story, givenStory);
        pendingStepStrategy = configuration.pendingStepStrategy();
        failureStrategy = configuration.failureStrategy();

        
        if (!filter.allow(story.getMeta())) {
            reporter.storyNotAllowed(story, filter.asString());
            return;
        }

        resetFailureState(givenStory);

        if (configuration.storyControls().dryRun() ){
            reporter.dryRun();
        }

        // run before story steps, if any
        reporter.beforeStory(story, givenStory);
        runBeforeOrAfterStorySteps(candidateSteps, story, givenStory, StepCollector.Stage.BEFORE);
        
        // determine if before and after scenario steps should be run
        boolean runBeforeAndAfterScenarioSteps = shouldRunBeforeOrAfterScenarioSteps(configuration, givenStory);
        
        for (Scenario scenario : story.getScenarios()) {
            // scenario also inherits meta from story
            if (!filter.allow(scenario.getMeta().inheritFrom(story.getMeta()))) {
                reporter.scenarioNotAllowed(scenario, filter.asString());
                continue;
            }
            if ( failureOccurred() && configuration.storyControls().skipScenariosAfterFailure() ){
                continue;
            }
            reporter.beforeScenario(scenario.getTitle());
            reporter.scenarioMeta(scenario.getMeta());

            // run before scenario steps, if allowed
            if (runBeforeAndAfterScenarioSteps) {
                runBeforeOrAfterScenarioSteps(candidateSteps, scenario, Stage.BEFORE);
            }

            // run given stories, if any
            runGivenStories(configuration, candidateSteps, scenario, filter);
            if (isParameterisedByExamples(scenario)) {
                // run parametrised scenarios by examples
                runParametrisedScenariosByExamples(candidateSteps, scenario);
            } else { // run as plain old scenario
                runScenarioSteps(candidateSteps, scenario, storyParameters);
            }

            // run after scenario steps, if allowed
            if (runBeforeAndAfterScenarioSteps) {
                runBeforeOrAfterScenarioSteps(candidateSteps, scenario, Stage.AFTER);
            }

            reporter.afterScenario();
        }

        // run after story steps, if any
        runBeforeOrAfterStorySteps(candidateSteps, story, givenStory, StepCollector.Stage.AFTER);        
        reporter.afterStory(givenStory);

        // handle any failure according to strategy
        if (!givenStory) {
            currentStrategy.handleFailure(storyFailure);
        }
    }

    private boolean shouldRunBeforeOrAfterScenarioSteps(Configuration configuration, boolean givenStory) {
        if (!configuration.storyControls().skipBeforeAndAfterScenarioStepsIfGivenStory()){
            return true;
        }
        
        return !givenStory;
    }

    private boolean failureOccurred() {
        return storyFailure != null;
    }

    private StoryReporter reporterFor(Configuration configuration, Story story, boolean givenStory) {
        if (givenStory) {
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

    private void runGivenStories(Configuration configuration, List<CandidateSteps> candidateSteps, Scenario scenario,
            MetaFilter filter) throws Throwable {
        GivenStories givenStories = scenario.getGivenStories();
        if (givenStories.getPaths().size() > 0) {
            reporter.givenStories(givenStories);
            for (GivenStory givenStory : givenStories.getStories()) {
                // run given story, using any parameters if provided
                Story story = storyOfPath(configuration, givenStory.getPath());
                run(configuration, candidateSteps, story, filter, true, givenStory.getParameters());
            }
        }
    }

    private boolean isParameterisedByExamples(Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() > 0 && !scenario.getGivenStories().requireParameters();
    }

    private void runParametrisedScenariosByExamples(List<CandidateSteps> candidateSteps, Scenario scenario) {
        ExamplesTable table = scenario.getExamplesTable();
        reporter.beforeExamples(scenario.getSteps(), table);
        for (Map<String, String> scenarioParameters : table.getRows()) {
            reporter.example(scenarioParameters);
            runScenarioSteps(candidateSteps, scenario, scenarioParameters);
        }
        reporter.afterExamples();
    }

    private void runBeforeOrAfterStorySteps(List<CandidateSteps> candidateSteps, Story story, boolean givenStory, Stage stage) {
        runStepsWhileKeepingState(stepCollector.collectBeforeOrAfterStorySteps(candidateSteps, story, stage, givenStory));
    }

    private void runBeforeOrAfterScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario, Stage stage) {
        runStepsWhileKeepingState(stepCollector.collectBeforeOrAfterScenarioSteps(candidateSteps, stage));
    }

    private void runScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> scenarioParameters) {
        runStepsWhileKeepingState(stepCollector.collectScenarioSteps(candidateSteps, scenario, scenarioParameters));
    }

    private void runStepsWhileKeepingState(List<Step> steps) {
        if (steps == null || steps.size() == 0){
            return;
        }
        state = new FineSoFar();
        for (Step step : steps) {
            state.run(step);
        }
    }

    private interface State {
        void run(Step step);
    }

    private final class FineSoFar implements State {

        public void run(Step step) {

            StepResult result = step.perform();
            result.describeTo(reporter);
            Throwable scenarioFailure = result.getFailure();
            if (scenarioFailure != null) {
                state = new SomethingHappened();
                storyFailure = mostImportantOf(storyFailure, scenarioFailure);
                currentStrategy = strategyFor(storyFailure);
            }
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
        public void run(Step step) {
            StepResult result = step.doNotPerform();
            result.describeTo(reporter);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
