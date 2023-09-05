package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.AbstractStepResult.Pending;
import org.jbehave.core.steps.StepCreator.PendingStep;

/**
 * StepCollector that marks unmatched steps as {@link Pending}. It uses a
 * {@link StepFinder} to prioritise {@link StepCandidate}s.
 */
public class MarkUnmatchedStepsAsPending implements StepCollector {

    private final StepFinder stepFinder;
    private final Keywords keywords;

    public MarkUnmatchedStepsAsPending() {
        this(new StepFinder());
    }

    public MarkUnmatchedStepsAsPending(StepFinder stepFinder) {
        this(stepFinder, new LocalizedKeywords());
    }

    public MarkUnmatchedStepsAsPending(StepFinder stepFinder, Keywords keywords) {
        this.stepFinder = stepFinder;
        this.keywords = keywords;
    }

    @Override
    public List<Step> collectBeforeOrAfterStoriesSteps(List<BeforeOrAfterStep> beforeOrAfterStoriesSteps) {
        return beforeOrAfterStoriesSteps.stream().map(BeforeOrAfterStep::createStep).collect(Collectors.toList());
    }

    @Override
    public List<Step> collectBeforeOrAfterStorySteps(List<BeforeOrAfterStep> beforeOrAfterStorySteps, Meta storyMeta) {
        return createSteps(beforeOrAfterStorySteps, storyMeta);
    }

    @Override
    public List<Step> collectBeforeScenarioSteps(List<BeforeOrAfterStep> beforeScenarioSteps,
            Meta storyAndScenarioMeta) {
        return createSteps(beforeScenarioSteps, storyAndScenarioMeta);
    }

    @Override
    public List<Step> collectAfterScenarioSteps(List<BeforeOrAfterStep> afterScenarioSteps, Meta storyAndScenarioMeta) {
        return afterScenarioSteps.stream().map(step -> step.createStepUponOutcome(storyAndScenarioMeta)).collect(
                Collectors.toList());
    }

    @Override
    public Map<Stage, List<Step>> collectLifecycleSteps(List<StepCandidate> stepCandidates, Lifecycle lifecycle,
            Meta storyAndScenarioMeta, Scope scope, Map<String, String> parameters, StepMonitor stepMonitor) {
        List<Step> beforeSteps = collectMatchedSteps(lifecycle.getBeforeSteps(scope), parameters, stepCandidates,
                null, stepMonitor);
        List<Step> afterSteps = Stream.of(Outcome.values())
                .map(outcome -> collectMatchedSteps(lifecycle.getAfterSteps(scope, outcome, storyAndScenarioMeta),
                        parameters, stepCandidates, outcome, stepMonitor))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Map<Stage, List<Step>> steps = new EnumMap<>(Stage.class);
        steps.put(Stage.BEFORE, beforeSteps);
        steps.put(Stage.AFTER, afterSteps);
        return steps;
    }

    @Override
    public List<Step> collectScenarioSteps(List<StepCandidate> stepCandidates, Scenario scenario,
            Map<String, String> parameters, StepMonitor stepMonitor) {
        return collectMatchedSteps(scenario.getSteps(), parameters, stepCandidates, null, stepMonitor);
    }

    private List<Step> createSteps(List<BeforeOrAfterStep> beforeOrAfterSteps, Meta meta) {
        return beforeOrAfterSteps.stream().map(step -> step.createStepWith(meta)).collect(Collectors.toList());
    }

    private List<Step> collectMatchedSteps(List<String> stepsAsString, Map<String, String> namedParameters,
            List<StepCandidate> stepCandidates, Outcome outcome, StepMonitor stepMonitor) {
        List<Step> steps = new ArrayList<>();
        String previousNonAndStep = null;
        for (String stepAsString : stepsAsString) {
            // pending is default step, overridden below
            Step step = StepCreator.createPendingStep(stepAsString, previousNonAndStep);
            List<StepCandidate> prioritisedCandidates = stepFinder.prioritise(stepAsString,
                    new ArrayList<>(stepCandidates));
            for (StepCandidate candidate : prioritisedCandidates) {
                candidate.useStepMonitor(stepMonitor);
                if (candidate.ignore(stepAsString)) {
                    // ignorable steps are added so they can be reported
                    step = StepCreator.createIgnorableStep(stepAsString);
                    break;
                }
                if (candidate.comment(stepAsString)) {
                    // comments are added so they can be reported
                    step = StepCreator.createComment(stepAsString);
                    break;
                }
                if (candidate.matches(stepAsString, previousNonAndStep)) {
                    // step matches candidate
                    if (candidate.isPending()) {
                        ((PendingStep) step).annotatedOn(candidate.getMethod());
                    } else {
                        List<Step> composedSteps = new ArrayList<>();
                        if (candidate.isComposite()) {
                            candidate.addComposedSteps(composedSteps, stepAsString, namedParameters,
                                    prioritisedCandidates);
                        }
                        if (outcome != null) {
                            step = candidate.createMatchedStepUponOutcome(stepAsString, namedParameters, composedSteps,
                                    outcome);
                        } else {
                            step = candidate.createMatchedStep(stepAsString, namedParameters, composedSteps);
                        }
                    }
                    if (!(keywords.isAndStep(stepAsString) || keywords.isIgnorableStep(stepAsString))) {
                        // only update previous step if not AND or IGNORABLE step
                        previousNonAndStep = stepAsString;
                    }
                    break;
                }
            }
            if (!(keywords.isAndStep(stepAsString) || keywords.isIgnorableStep(stepAsString))) {
                previousNonAndStep = stepAsString;
            }
            steps.add(step);
        }
        return steps;
    }
}
