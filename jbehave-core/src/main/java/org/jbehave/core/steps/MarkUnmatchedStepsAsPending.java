package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.AbstractStepResult.Pending;

/**
 * StepCollector that marks unmatched steps as {@link Pending}. It uses a
 * {@link StepFinder} to collect and prioritise {@link StepCandidate}s.
 */
public class MarkUnmatchedStepsAsPending implements StepCollector {

	private final StepFinder stepFinder;

    public MarkUnmatchedStepsAsPending() {
		this(new StepFinder());
	}

	public MarkUnmatchedStepsAsPending(StepFinder stepFinder) {
		this.stepFinder = stepFinder;
	}

    public List<Step> collectBeforeOrAfterStoriesSteps(List<CandidateSteps> candidateSteps, Stage stage) {
        List<Step> steps = new ArrayList<Step>();
        for (CandidateSteps candidates : candidateSteps) {
            steps.addAll(createSteps(candidates
                    .listBeforeOrAfterStories(), stage));
        }
        return steps;
    }

	public List<Step> collectBeforeOrAfterStorySteps(List<CandidateSteps> candidateSteps,
			Story story, Stage stage, boolean givenStory) {
		List<Step> steps = new ArrayList<Step>();
		for (CandidateSteps candidates : candidateSteps) {
			steps.addAll(createSteps(candidates
					.listBeforeOrAfterStory(givenStory), stage));
		}
		return steps;
	}

	private List<Step> createSteps(List<BeforeOrAfterStep> beforeOrAfter,
			Stage stage) {
		List<Step> steps = new ArrayList<Step>();
		for (BeforeOrAfterStep step : beforeOrAfter) {
			if (stage == step.getStage()) {
				steps.add(step.createStep());
			}
		}
		return steps;
	}

	public List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps,
			Scenario scenario, Map<String, String> tableRow) {
		List<Step> steps = new ArrayList<Step>();

		addMatchedScenarioSteps(scenario, steps, tableRow, candidateSteps);
		addBeforeAndAfterScenarioSteps(steps, candidateSteps);

		return steps;
	}

	private void addBeforeAndAfterScenarioSteps(List<Step> steps,
			List<CandidateSteps> candidateSteps) {
		for (CandidateSteps candidates : candidateSteps) {
			steps.addAll(0, createSteps(candidates.listBeforeOrAfterScenario(),
					Stage.BEFORE));
		}

		for (CandidateSteps candidates : candidateSteps) {
			steps.addAll(createStepsUponOutcome(candidates
					.listBeforeOrAfterScenario(), Stage.AFTER));
		}
	}

	private List<Step> createStepsUponOutcome(
			List<BeforeOrAfterStep> beforeOrAfter, Stage stage) {
		List<Step> steps = new ArrayList<Step>();
		for (BeforeOrAfterStep step : beforeOrAfter) {
			if (stage == step.getStage()) {
				steps.add(step.createStepUponOutcome());
			}
		}
		return steps;
	}

	private void addMatchedScenarioSteps(Scenario scenario, List<Step> steps,
			Map<String, String> tableRow, List<CandidateSteps> candidateSteps) {
        List<StepCandidate> allCandidates = stepFinder.collectCandidates(candidateSteps);
		String previousNonAndStep = null;
		for (String stepAsString : scenario.getSteps()) {
			// pending is default step, overridden below
			Step step = StepCreator.createPendingStep(stepAsString);
			for (StepCandidate candidate : stepFinder.prioritise(stepAsString, allCandidates)) {
				if (candidate.ignore(stepAsString)) {
					// ignorable steps are added
					// so they can be reported
					step = StepCreator.createIgnorableStep(stepAsString);
					break;
				}
				if (matchesCandidate(stepAsString, previousNonAndStep,
						candidate)) {
					step = candidate.createMatchedStep(stepAsString, tableRow);
					if (!candidate.isAndStep(stepAsString)) {
						// only update previous step if not AND step
						previousNonAndStep = stepAsString;
					}
					break;
				}
			}
			steps.add(step);
		}
	}

	private boolean matchesCandidate(String step, String previousNonAndStep,
			StepCandidate candidate) {
		if (previousNonAndStep != null) {
			return candidate.matches(step, previousNonAndStep);
		}
		return candidate.matches(step);
	}

}
