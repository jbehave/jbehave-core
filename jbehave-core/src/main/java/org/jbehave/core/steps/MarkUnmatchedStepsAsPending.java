package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.AbstractStepResult.Pending;

/**
 * StepCollector that marks unmatched steps as {@link Pending}
 */
public class MarkUnmatchedStepsAsPending implements StepCollector {

	private CandidateStepFinder stepFinder = new CandidateStepFinder();
	
    public List<Step> collectStepsFrom(List<CandidateSteps> candidateSteps, Story story, Stage stage, boolean givenStory) {
        List<Step> steps = new ArrayList<Step>();
        for (CandidateSteps candidates : candidateSteps) {
            switch (stage) {
                case BEFORE:
                    steps.addAll(candidates.runBeforeStory(givenStory));
                    break;
                case AFTER:
                    steps.addAll(candidates.runAfterStory(givenStory));
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
        List<CandidateStep> prioritisedCandidates = stepFinder.collectAndPrioritise(candidateSteps);
        String previousNonAndStep = null;
        for (String stepAsString : scenario.getSteps()) {        
        	// pending is default step, overridden below
            Step step = StepCreator.createPendingStep(stepAsString);
            for (CandidateStep candidate : prioritisedCandidates) {
                if (candidate.ignore(stepAsString)) { 
                	// ignorable steps are added
                    // so they can be reported
                    step = StepCreator.createIgnorableStep(stepAsString);
                    break;
                }
                if (matchesCandidate(stepAsString, previousNonAndStep, candidate)) {
                    step = candidate.createMatchedStep(stepAsString, tableRow);
                    if  ( !candidate.isAndStep(stepAsString) ){
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
			CandidateStep candidate) {
		if ( previousNonAndStep != null ){
			return candidate.matches(step, previousNonAndStep);			
		}
		return candidate.matches(step);
	}

   

}
