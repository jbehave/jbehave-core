package org.jbehave.core.embedder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepResult;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * Runs a {@link Story}, given a {@link Configuration} and a list of {@link CandidateSteps}, 
 * describing the results to the {@link StoryReporter}.
 *
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 * @author Paul Hammant
 */
public class StoryRunner {

    private State state = new FineSoFar();
    private FailureStrategy currentStrategy;
    private PendingStepStrategy pendingStepStrategy;
    private StoryReporter reporter;
    private FailureStrategy failureStrategy;
    private Throwable storyFailure;
    private StepCollector stepCollector;
	private String reporterStoryPath;

	/**
	 * Run steps before or after a collection of stories.  Steps are execute only <b>once</b> per collection
	 * of stories.
	 * 
     * @param configuration the Configuration used to find the steps to run
	 * @param candidateSteps List of CandidateSteps containing the candidate steps methods
	 * @param stage the Stage
	 */
    public void runBeforeOrAfterStories(Configuration configuration, List<CandidateSteps> candidateSteps, Stage stage) {
        runSteps(configuration.stepCollector().collectBeforeOrAfterStoriesSteps(candidateSteps, stage));        
    }

    /**
	 * Runs a Story with the given configuration and steps.  
	 * 
	 * @param configuration the Configuration used to run story
	 * @param candidateSteps the List of CandidateSteps containing the candidate steps methods
	 * @param story the Story to run
	 * @throws Throwable if failures occurred and FailureStrategy dictates it to be re-thrown.
	 */
    public void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story) throws Throwable {
        run(configuration, candidateSteps, story, false);
    }

    private void run(Configuration configuration, List<CandidateSteps> candidateSteps, Story story, boolean givenStory) throws Throwable {
        stepCollector = configuration.stepCollector();
        reporter = reporterFor(configuration, story, givenStory);
        pendingStepStrategy = configuration.pendingStepStrategy();
        failureStrategy = configuration.failureStrategy();
        resetFailureState(givenStory);

		if (isDryRun(candidateSteps)) {
			reporter.dryRun();
		}

        reporter.beforeStory(story, givenStory);
        runStorySteps(candidateSteps, story, givenStory, StepCollector.Stage.BEFORE);
        for (Scenario scenario : story.getScenarios()) {
            reporter.beforeScenario(scenario.getTitle());
            runGivenStories(configuration, candidateSteps, scenario); // first run any given stories, if any
            if (isExamplesTableScenario(scenario)) { // run as examples table scenario
                runExamplesTableScenario(candidateSteps, scenario);
            } else { // run as plain old scenario
                runScenarioSteps(candidateSteps, scenario, new HashMap<String, String>());
            }
            reporter.afterScenario();
        }
        runStorySteps(candidateSteps, story, givenStory, StepCollector.Stage.AFTER);
        reporter.afterStory(givenStory);
        currentStrategy.handleFailure(storyFailure);
    }
    
    public Story storyOfPath(Configuration configuration, String storyPath) {
        String storyAsString = configuration.storyLoader().loadStoryAsText(storyPath);
        Story story = configuration.storyParser().parseStory(storyAsString, storyPath);
        story.namedAs(new File(storyPath).getName());
        return story;
    }

	private boolean isDryRun(List<CandidateSteps> candidateSteps) {
		for (CandidateSteps steps : candidateSteps) {
			if (steps.configuration().dryRun()) {
				return true;
			}
		}
		return false;
	}

	private StoryReporter reporterFor(Configuration configuration,
			Story story, boolean givenStory) {
		if ( givenStory ){			
			return configuration.storyReporter(reporterStoryPath);
		} else {
			// store parent story path for reporting
			reporterStoryPath = story.getPath();
			return configuration.storyReporter(reporterStoryPath);
		}
	}

	private void resetFailureState(boolean givenStory) {
		if ( givenStory ) {
			// do not reset failure state for given stories
			return;
		}
		currentStrategy = new SilentlyAbsorbingFailure();
		storyFailure = null;
	}
	
    private void runGivenStories(Configuration configuration,
                                 List<CandidateSteps> candidateSteps, Scenario scenario)
            throws Throwable {
        List<String> storyPaths = scenario.getGivenStoryPaths();
        if (storyPaths.size() > 0) {
            reporter.givenStories(storyPaths);
            for (String storyPath : storyPaths) {
                // run given story 
                Story story = storyOfPath(configuration, storyPath);
                run(configuration, candidateSteps, story, true);
            }
        }
    }

    private boolean isExamplesTableScenario(Scenario scenario) {
        return scenario.getExamplesTable().getRowCount() > 0;
    }

    private void runExamplesTableScenario(
            List<CandidateSteps> candidateSteps, Scenario scenario) {
        ExamplesTable table = scenario.getExamplesTable();
        reporter.beforeExamples(scenario.getSteps(), table);
        for (Map<String, String> tableRow : table.getRows()) {
            reporter.example(tableRow);
            runScenarioSteps(candidateSteps, scenario, tableRow);
        }
        reporter.afterExamples();
    }

    private void runStorySteps(List<CandidateSteps> candidateSteps, Story story, boolean givenStory, Stage stage) {
        runSteps(stepCollector.collectBeforeOrAfterStorySteps(candidateSteps, story, stage, givenStory));
    }

    private void runScenarioSteps(
            List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> tableRow) {
        runSteps(stepCollector.collectScenarioSteps(candidateSteps, scenario, tableRow));
    }

    /**
     * Runs a list of steps, while keeping state
     *
     * @param steps the Steps to run
     */
    private void runSteps(List<Step> steps) {
        if (steps == null || steps.size() == 0) return;
        state = new FineSoFar();
        for (Step step : steps) {
            state.run(step);
        }
    }

    private class SomethingHappened implements State {
        public void run(Step step) {
            StepResult result = step.doNotPerform();
            result.describeTo(reporter);
        }
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

        private Throwable mostImportantOf(
                Throwable failure1,
                Throwable failure2) {
            return failure1 == null ? failure2 :
                    failure1 instanceof PendingStepFound ? (failure2 == null ? failure1 : failure2) :
                            failure1;
        }

        private FailureStrategy strategyFor(Throwable failure) {
            if (failure instanceof PendingStepFound) {
                return pendingStepStrategy;
            } else {
                return failureStrategy;
            }
        }
    }

    private interface State {
        void run(Step step);
    }
    
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}


}
