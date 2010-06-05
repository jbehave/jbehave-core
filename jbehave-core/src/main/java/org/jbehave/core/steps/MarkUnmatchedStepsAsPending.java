package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

/**
 * StepCollector that marks unmatched steps as {@link StepResult.Pending}
 */
public class MarkUnmatchedStepsAsPending implements StepCollector {

    public List<Step> collectStepsFrom(List<CandidateSteps> candidateSteps, Story story, Stage stage, boolean embeddedStory) {
        List<Step> steps = new ArrayList<Step>();
        for (CandidateSteps candidates : candidateSteps) {
            switch (stage) {
                case BEFORE:
                    steps.addAll(candidates.runBeforeStory(embeddedStory));
                    break;
                case AFTER:
                    steps.addAll(candidates.runAfterStory(embeddedStory));
                    break;
                default:
                    break;
            }
        }
        return steps;
    }

    public List<Step> collectStepsFrom(List<CandidateSteps> candidateSteps, Scenario scenario,
                                      Map<String, String> tableRow) {
        List<Step> steps = new ArrayList<Step>();

        addMatchedScenarioSteps(scenario, steps, tableRow, candidateSteps);
        addBeforeAndAfterScenarioSteps(steps, candidateSteps);

        return steps;
    }

    private void addBeforeAndAfterScenarioSteps(List<Step> steps, List<CandidateSteps> candidateSteps) {
        for (CandidateSteps candidates : candidateSteps) {
            steps.addAll(0, candidates.runBeforeScenario());
        }

        for (CandidateSteps candidates : candidateSteps) {
            steps.addAll(candidates.runAfterScenario());
        }
    }

    private void addMatchedScenarioSteps(Scenario scenario, List<Step> steps,
                                         Map<String, String> tableRow, List<CandidateSteps> candidateSteps) {
        List<CandidateStep> prioritised = prioritise(candidateSteps);
        String previousNonAndStep = null;
        for (String stringStep : scenario.getSteps()) {        	
            Step step = new PendingStep(stringStep);
            for (CandidateStep candidate : prioritised) {
                if (candidate.ignore(stringStep)) { // ignorable steps are added
                                                    // so they can be reported
                    step = new IgnorableStep(stringStep);
                    break;
                }
                if (matchesCandidate(stringStep, previousNonAndStep, candidate)) {
                    step = candidate.createFrom(tableRow, stringStep);
                    if  ( !candidate.isAndStep(stringStep) ){
                    	// only update previous step if not AND step
                        previousNonAndStep = stringStep;            	
                    }
                    break;
                }
            }
            steps.add(step);
        }
    }

	private boolean matchesCandidate(String step, String previousNonAndStep,
			CandidateStep candidate) {
		if ( previousNonAndStep != null ){
			return candidate.matches(step, previousNonAndStep);			
		}
		return candidate.matches(step);
	}

    private List<CandidateStep> prioritise(List<CandidateSteps> candidateSteps) {
        List<CandidateStep> steps = new ArrayList<CandidateStep>();
        for (CandidateSteps candidates : candidateSteps) {
            steps.addAll(asList(candidates.getSteps()));
        }
        Collections.sort(steps, new Comparator<CandidateStep>() {
            public int compare(CandidateStep o1, CandidateStep o2) {
                // sort by decreasing order of priority
                return -1 * o1.getPriority().compareTo(o2.getPriority());
            }
        });
        return steps;
    }

}
