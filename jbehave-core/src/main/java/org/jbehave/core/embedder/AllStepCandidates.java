package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;

public class AllStepCandidates
{
    private final List<BeforeOrAfterStep> beforeStoriesSteps = new ArrayList<>();
    private final List<BeforeOrAfterStep> afterStoriesSteps = new ArrayList<>();

    private final List<BeforeOrAfterStep> beforeGivenStorySteps = new ArrayList<>();
    private final List<BeforeOrAfterStep> afterGivenStorySteps = new ArrayList<>();

    private final List<BeforeOrAfterStep> beforeStorySteps = new ArrayList<>();
    private final List<BeforeOrAfterStep> afterStorySteps = new ArrayList<>();

    private final Map<ScenarioType, List<BeforeOrAfterStep>> beforeScenarioSteps = new EnumMap<>(ScenarioType.class);
    private final Map<ScenarioType, List<BeforeOrAfterStep>> afterScenarioSteps = new EnumMap<>(ScenarioType.class);

    private final List<StepCandidate> regularSteps = new ArrayList<>();

    public AllStepCandidates(List<CandidateSteps> candidateSteps) {
        for (ScenarioType type : ScenarioType.values()) {
            beforeScenarioSteps.put(type, new ArrayList<>());
            afterScenarioSteps.put(type, new ArrayList<>());
        }
        for (CandidateSteps candidateStep : candidateSteps) {
            beforeStoriesSteps.addAll(candidateStep.listBeforeStories());
            afterStoriesSteps.addAll(candidateStep.listAfterStories());

            beforeGivenStorySteps.addAll(candidateStep.listBeforeStory(true));
            afterGivenStorySteps.addAll(candidateStep.listAfterStory(true));

            beforeStorySteps.addAll(candidateStep.listBeforeStory(false));
            afterStorySteps.addAll(candidateStep.listAfterStory(false));

            candidateStep.listBeforeScenario().forEach(
                    (scenarioType, steps) -> beforeScenarioSteps.get(scenarioType).addAll(steps));
            candidateStep.listAfterScenario().forEach(
                    (scenarioType, steps) -> afterScenarioSteps.get(scenarioType).addAll(steps));

            regularSteps.addAll(candidateStep.listCandidates());
        }

        sortBeforeSteps(beforeStoriesSteps);
        sortAfterSteps(afterStoriesSteps);

        sortBeforeSteps(beforeGivenStorySteps);
        sortAfterSteps(afterGivenStorySteps);

        sortBeforeSteps(beforeStorySteps);
        sortAfterSteps(afterStorySteps);

        beforeScenarioSteps.values().forEach(this::sortBeforeSteps);
        afterScenarioSteps.values().forEach(this::sortAfterSteps);
    }

    private void sortBeforeSteps(List<BeforeOrAfterStep> beforeSteps) {
        sortSteps(beforeSteps, Comparator.reverseOrder());
    }

    private void sortAfterSteps(List<BeforeOrAfterStep> afterSteps) {
        sortSteps(afterSteps, Comparator.naturalOrder());
    }

    private void sortSteps(List<BeforeOrAfterStep> steps, Comparator<Integer> comparator) {
        steps.sort(Comparator.comparing(BeforeOrAfterStep::getOrder, comparator));
    }

    public List<BeforeOrAfterStep> getBeforeStoriesSteps() {
        return beforeStoriesSteps;
    }

    public List<BeforeOrAfterStep> getAfterStoriesSteps() {
        return afterStoriesSteps;
    }

    public List<BeforeOrAfterStep> getBeforeStorySteps(boolean givenStory) {
        return givenStory ? beforeGivenStorySteps : beforeStorySteps;
    }

    public List<BeforeOrAfterStep> getAfterStorySteps(boolean givenStory) {
        return givenStory ? afterGivenStorySteps: afterStorySteps;
    }

    public List<BeforeOrAfterStep> getBeforeScenarioSteps(ScenarioType scenarioType) {
        return beforeScenarioSteps.get(scenarioType);
    }

    public List<BeforeOrAfterStep> getAfterScenarioSteps(ScenarioType scenarioType) {
        return afterScenarioSteps.get(scenarioType);
    }

    public List<StepCandidate> getRegularSteps() {
        return regularSteps;
    }
}
