package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Finds candidate steps matching a textual step from a list of
 * {@link CandidateSteps} instances.
 */
public class CandidateStepFinder {

	public List<Stepdoc> stepdocs(List<CandidateSteps> steps) {
		List<Stepdoc> stepdocs = new LinkedList<Stepdoc>();
		for (CandidateStep candidateStep : collectAndPrioritise(steps)){
			stepdocs.add(new Stepdoc(candidateStep));
		}
		Collections.sort(stepdocs);
		return stepdocs;
	}

	public List<Stepdoc> findMatching(String stepAsString,
			List<CandidateSteps> candidateSteps) {
		List<Stepdoc> matching = new ArrayList<Stepdoc>();
		for (CandidateStep candidate : collect(candidateSteps)) {
			if (candidate.matches(stepAsString)) {
				matching.add(new Stepdoc(candidate));
			}
		}
		return matching;
	}

	public List<Object> stepsInstances(List<CandidateSteps> candidateSteps) {
		List<Object> instances = new ArrayList<Object>();
		for (CandidateSteps steps : candidateSteps) {
			if (steps instanceof Steps) {
				instances.add(((Steps) steps).instance());
			}
		}
		return instances;
	}

	public List<CandidateStep> collectAndPrioritise(
			List<CandidateSteps> candidateSteps) {
		return prioritise(collect(candidateSteps));
	}

	private List<CandidateStep> collect(List<CandidateSteps> candidateSteps) {
		List<CandidateStep> collected = new ArrayList<CandidateStep>();
		for (CandidateSteps candidates : candidateSteps) {
			collected.addAll(candidates.listCandidates());
		}
		return collected;
	}

	private List<CandidateStep> prioritise(List<CandidateStep> candidateSteps) {
		List<CandidateStep> prioritised = new ArrayList<CandidateStep>(
				candidateSteps);
		Collections.sort(prioritised, new Comparator<CandidateStep>() {
			public int compare(CandidateStep o1, CandidateStep o2) {
				// sort by decreasing order of priority
				return -1 * o1.getPriority().compareTo(o2.getPriority());
			}
		});
		return prioritised;
	}

}
