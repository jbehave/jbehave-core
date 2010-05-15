package org.jbehave.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.PendingError;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepResult;
import org.jbehave.core.steps.StepCreator.Stage;

/**
 * Allows to run a single story and describe the results to the {@link StoryReporter}.
 *
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 * @author Paul Hammant
 */
public class StoryRunner {

    private State state = new FineSoFar();
    private ErrorStrategy currentStrategy;
    private PendingErrorStrategy pendingStepStrategy;
    private StoryReporter reporter;
    private ErrorStrategy errorStrategy;
    private Throwable throwable;
    private StepCreator stepCreator;
	private String reporterStoryPath;

    public void run(StoryConfiguration configuration, List<CandidateSteps> candidateSteps, Class<? extends RunnableStory> storyClass) throws Throwable {
        String storyPath = configuration.storyPathResolver().resolve(storyClass);
        run(configuration, candidateSteps, storyPath);
    }

    public void run(StoryConfiguration configuration, List<CandidateSteps> candidateSteps, String storyPath) throws Throwable {
        Story story = defineStory(configuration, storyPath);
        run(configuration, candidateSteps, story);
    }

    public void run(StoryConfiguration configuration, List<CandidateSteps> candidateSteps, Story story) throws Throwable {
        // always start in a non-embedded mode
        run(configuration, candidateSteps, story, false);
    }

    public void run(StoryConfiguration configuration, List<CandidateSteps> candidateSteps, String storyPath, boolean embeddedStory) throws Throwable {
        Story story = defineStory(configuration, storyPath);
        run(configuration, candidateSteps, story, embeddedStory);
    }

    public Story defineStory(StoryConfiguration storyConfiguration, String storyPath) {
        String storyAsString = storyConfiguration.storyLoader().loadStoryAsText(storyPath);
        Story story = storyConfiguration.storyParser().parseStory(storyAsString, storyPath);
        story.namedAs(new File(storyPath).getName());
        return story;
    }

    public void run(StoryConfiguration configuration, List<CandidateSteps> candidateSteps, Story story, boolean embeddedStory) throws Throwable {
        stepCreator = configuration.stepCreator();
        reporter = reporterFor(configuration, story, embeddedStory);
        pendingStepStrategy = configuration.pendingErrorStrategy();
        errorStrategy = configuration.errorStrategy();
        resetErrorState(embeddedStory);

		if (isDryRun(candidateSteps)) {
			reporter.dryRun();
		}

        reporter.beforeStory(story, embeddedStory);
        runStorySteps(candidateSteps, story, embeddedStory, StepCreator.Stage.BEFORE);
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
        runStorySteps(candidateSteps, story, embeddedStory, StepCreator.Stage.AFTER);
        reporter.afterStory(embeddedStory);
        currentStrategy.handleError(throwable);
    }
    
	private boolean isDryRun(List<CandidateSteps> candidateSteps) {
		for (CandidateSteps steps : candidateSteps) {
			if (steps.getConfiguration().dryRun()) {
				return true;
			}
		}
		return false;
	}

	private StoryReporter reporterFor(StoryConfiguration configuration,
			Story story, boolean embeddedStory) {
		if ( embeddedStory ){			
			return configuration.storyReporter(reporterStoryPath);
		} else {
			// store parent story path for reporting
			reporterStoryPath = story.getPath();
			return configuration.storyReporter(reporterStoryPath);
		}
	}

	private void resetErrorState(boolean embeddedStory) {
		if ( embeddedStory ) {
			// do not reset error state for embedded stories
			return;
		}
		currentStrategy = ErrorStrategy.SILENT;
		throwable = null;
	}
	
    private void runGivenStories(StoryConfiguration configuration,
                                 List<CandidateSteps> candidateSteps, Scenario scenario)
            throws Throwable {
        // run given story in embedded mode
        List<String> storyPaths = scenario.getGivenStoryPaths();
        if (storyPaths.size() > 0) {
            reporter.givenStories(storyPaths);
            for (String storyPath : storyPaths) {
                run(configuration, candidateSteps, storyPath, true);
            }
        }
    }

    private boolean isExamplesTableScenario(Scenario scenario) {
        return scenario.getTable().getRowCount() > 0;
    }

    private void runExamplesTableScenario(
            List<CandidateSteps> candidateSteps, Scenario scenario) {
        ExamplesTable table = scenario.getTable();
        reporter.beforeExamples(scenario.getSteps(), table);
        for (Map<String, String> tableRow : table.getRows()) {
            reporter.example(tableRow);
            runScenarioSteps(candidateSteps, scenario, tableRow);
        }
        reporter.afterExamples();
    }

    private void runStorySteps(List<CandidateSteps> candidateSteps, Story story, boolean embeddedStory, Stage stage) {
        runSteps(stepCreator.createStepsFrom(candidateSteps, story, stage, embeddedStory));
    }

    private void runScenarioSteps(
            List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> tableRow) {
        runSteps(stepCreator.createStepsFrom(candidateSteps, scenario, tableRow));
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
            Throwable thisScenariosThrowable = result.getThrowable();
            if (thisScenariosThrowable != null) {
                state = new SomethingHappened();
                throwable = mostImportantOf(throwable, thisScenariosThrowable);
                currentStrategy = strategyFor(throwable);
            }
        }

        private Throwable mostImportantOf(
                Throwable throwable1,
                Throwable throwable2) {
            return throwable1 == null ? throwable2 :
                    throwable1 instanceof PendingError ? (throwable2 == null ? throwable1 : throwable2) :
                            throwable1;
        }

        private ErrorStrategy strategyFor(Throwable throwable) {
            if (throwable instanceof PendingError) {
                return pendingStepStrategy;
            } else {
                return errorStrategy;
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
