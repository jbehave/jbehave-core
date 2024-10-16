package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;

import org.jbehave.core.annotations.Conditional;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.condition.StepConditionMatcher;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ConditionalStepCandidate;
import org.jbehave.core.steps.StepCandidate;

public class AllStepCandidates {
    private final List<BeforeOrAfterStep> beforeStoriesSteps = new ArrayList<>();
    private final List<BeforeOrAfterStep> afterStoriesSteps = new ArrayList<>();

    private final List<BeforeOrAfterStep> beforeGivenStorySteps = new ArrayList<>();
    private final List<BeforeOrAfterStep> afterGivenStorySteps = new ArrayList<>();

    private final List<BeforeOrAfterStep> beforeStorySteps = new ArrayList<>();
    private final List<BeforeOrAfterStep> afterStorySteps = new ArrayList<>();

    private final Map<ScenarioType, List<BeforeOrAfterStep>> beforeScenarioSteps = new EnumMap<>(ScenarioType.class);
    private final Map<ScenarioType, List<BeforeOrAfterStep>> afterScenarioSteps = new EnumMap<>(ScenarioType.class);

    private final List<StepCandidate> regularSteps;

    public AllStepCandidates(StepConditionMatcher stepConditionMatcher, List<CandidateSteps> candidateSteps) {

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
        }

        this.regularSteps = candidateSteps.stream()
                .map(CandidateSteps::listCandidates)
                .flatMap(List::stream)
                .collect(stepCandidateCollector())
                .entrySet()
                .stream()
                .map(e -> {
                    List<StepCandidate> candidates = e.getValue();
                    boolean allCandidatesConditional = areAllCandidatesConditional(candidates);
                    if (allCandidatesConditional) {
                        return ConditionalStepCandidate.from(stepConditionMatcher, candidates);
                    }
                    if (candidates.size() == 1) {
                        return candidates.get(0);
                    }
                    throw new DuplicateCandidateFound(e.getKey());
                })
                .collect(Collectors.toList());

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
        return givenStory ? afterGivenStorySteps : afterStorySteps;
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

    private static Collector<StepCandidate, Map<String, List<StepCandidate>>, Map<String, List<StepCandidate>>>
        stepCandidateCollector() {
        return Collector.of(LinkedHashMap::new, (map, candidate) -> {

            String candidateWording = candidate.getStartingWord() + " " + candidate.getPatternAsString();

            Optional<String> candidateKey = map.keySet().stream()
                    .filter(k -> candidate.matches(k) && map.get(k).stream().allMatch(c -> c.matches(candidateWording)))
                    .findFirst();

            List<StepCandidate> candidates;
            if (candidateKey.isPresent()) {
                candidates = map.get(candidateKey.get());
            } else {
                candidates = new ArrayList<>();
                map.put(candidateWording, candidates);
            }
            candidates.add(candidate);
        }, (l, r) -> l, Characteristics.IDENTITY_FINISH);
    }

    private boolean areAllCandidatesConditional(Collection<StepCandidate> candidates) {
        return candidates.stream()
                         .map(StepCandidate::getMethod)
                         .allMatch(m -> m != null
                             && (m.isAnnotationPresent(Conditional.class)
                                     || m.getDeclaringClass().isAnnotationPresent(Conditional.class)));
    }

}
