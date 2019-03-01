package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.AbstractStepResult.Pending;
import org.jbehave.core.steps.StepCreator.PendingStep;

/**
 * StepCollector that marks unmatched steps as {@link Pending}. It uses a
 * {@link StepFinder} to collect and prioritise {@link StepCandidate}s.
 */
public class MarkUnmatchedStepsAsPending implements StepCollector {

    private static final StepMonitor DEFAULT_STEP_MONITOR = new NullStepMonitor();

    private final StepFinder stepFinder;
    private final Keywords keywords;

    public MarkUnmatchedStepsAsPending() {
        this(new StepFinder());
    }

    public MarkUnmatchedStepsAsPending(StepFinder stepFinder) {
        this(stepFinder, new LocalizedKeywords());
    }

    public MarkUnmatchedStepsAsPending(Keywords keywords) {
        this(new StepFinder(), keywords);
    }

    public MarkUnmatchedStepsAsPending(StepFinder stepFinder, Keywords keywords) {
        this.stepFinder = stepFinder;
        this.keywords = keywords;
    }

     @Override
     public List<Step> collectBeforeOrAfterStoriesSteps(List<CandidateSteps> candidateSteps, Stage stage) {
        List<Step> steps = new ArrayList<>();
        for (CandidateSteps candidates : candidateSteps) {
            steps.addAll(createSteps(candidates.listBeforeOrAfterStories(), stage));
        }
        return steps;
    }

    @Override
    public List<Step> collectBeforeOrAfterStorySteps(List<CandidateSteps> candidateSteps, Story story, Stage stage,
            boolean givenStory) {
        List<Step> steps = new ArrayList<>();
        for (CandidateSteps candidates : candidateSteps) {
            steps.addAll(createSteps(candidates.listBeforeOrAfterStory(givenStory), story.getMeta(), stage));
        }
        return steps;
    }

    @Override
    public List<Step> collectBeforeOrAfterScenarioSteps(List<CandidateSteps> candidateSteps, Meta storyAndScenarioMeta, Stage stage, ScenarioType type) {
        List<Step> steps = new ArrayList<>();
        for (CandidateSteps candidates : candidateSteps) {
            List<BeforeOrAfterStep> beforeOrAfterScenarioSteps = candidates.listBeforeOrAfterScenario(type);
            if (stage == Stage.BEFORE) {
                steps.addAll(createSteps(beforeOrAfterScenarioSteps, storyAndScenarioMeta, stage));
            } else {
                steps.addAll(0, createStepsUponOutcome(beforeOrAfterScenarioSteps, storyAndScenarioMeta, stage));
            }
        }
        return steps;
    }

    @Override
    @Deprecated
    public List<Step> collectLifecycleSteps(List<CandidateSteps> candidateSteps, Lifecycle lifecycle, Meta storyAndScenarioMeta, Stage stage) {
        return collectLifecycleSteps(candidateSteps, lifecycle, storyAndScenarioMeta, stage, Scope.SCENARIO);
    }

    @Override
    @Deprecated
    public List<Step> collectLifecycleSteps(List<CandidateSteps> candidateSteps, Lifecycle lifecycle, Meta storyAndScenarioMeta, Stage stage, Scope scope) {
        List<StepCandidate> allCandidates = stepFinder.collectCandidates(candidateSteps);
        List<Step> steps = new ArrayList<>();
        Map<String, String> namedParameters = new HashMap<>();
        if (stage == Stage.BEFORE) {
            steps.addAll(collectLifecycleBeforeSteps(allCandidates, lifecycle, scope, namedParameters));
        }
        else {
            steps.addAll(
                    collectLifecycleAfterSteps(allCandidates, lifecycle, storyAndScenarioMeta, scope, namedParameters));
        }
        return steps;
    }

    @Override
    public Map<Stage, List<Step>> collectLifecycleSteps(List<CandidateSteps> candidateSteps, Lifecycle lifecycle, Meta storyAndScenarioMeta, Scope scope) {
        List<StepCandidate> allCandidates = stepFinder.collectCandidates(candidateSteps);
        Map<String, String> namedParameters = new HashMap<>();
        Map<Stage, List<Step>> steps = new EnumMap<>(Stage.class);
        steps.put(Stage.BEFORE, collectLifecycleBeforeSteps(allCandidates, lifecycle, scope, namedParameters));
        steps.put(Stage.AFTER,
                collectLifecycleAfterSteps(allCandidates, lifecycle, storyAndScenarioMeta, scope, namedParameters));
        return steps;
    }

    @Override
    public List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario,
            Map<String, String> parameters) {
        return collectScenarioSteps(candidateSteps, scenario, parameters, DEFAULT_STEP_MONITOR);
    }

    @Override
    public List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario,
            Map<String, String> parameters, StepMonitor stepMonitor) {
        List<Step> steps = new ArrayList<>();
        addMatchedSteps(scenario.getSteps(), steps, parameters, stepFinder.collectCandidates(candidateSteps), null,
                stepMonitor);
        return steps;
    }

    private List<Step> createSteps(List<BeforeOrAfterStep> beforeOrAfter, Stage stage) {
        return createSteps(beforeOrAfter, null, stage);
    }

    private List<Step> createSteps(List<BeforeOrAfterStep> beforeOrAfter, Meta meta, Stage stage) {
        List<Step> steps = new ArrayList<>();
        for (BeforeOrAfterStep step : beforeOrAfter) {
            if (stage == step.getStage()) {
                steps.add(meta == null ? step.createStep() : step.createStepWith(meta));
            }
        }
        return steps;
    }

    private List<Step> createStepsUponOutcome(List<BeforeOrAfterStep> beforeOrAfter, Meta storyAndScenarioMeta, Stage stage) {
        List<Step> steps = new ArrayList<>();
        for (BeforeOrAfterStep step : beforeOrAfter) {
            if (stage == step.getStage()) {
                steps.add(step.createStepUponOutcome(storyAndScenarioMeta));
            }
        }
        return steps;
    }

    private List<Step> collectLifecycleBeforeSteps(List<StepCandidate> allCandidates, Lifecycle lifecycle, Scope scope,
            Map<String, String> namedParameters) {
        List<Step> beforeSteps = new ArrayList<>();
        addMatchedSteps(lifecycle.getBeforeSteps(scope), beforeSteps, namedParameters, allCandidates, null);
        return beforeSteps;
    }

    private List<Step> collectLifecycleAfterSteps(List<StepCandidate> allCandidates, Lifecycle lifecycle,
            Meta storyAndScenarioMeta, Scope scope, Map<String, String> namedParameters) {
        List<Step> afterSteps = new ArrayList<>();
        for (Outcome outcome : Outcome.values()) {
            addMatchedSteps(lifecycle.getAfterSteps(scope, outcome, storyAndScenarioMeta), afterSteps, namedParameters,
                    allCandidates, outcome);
        }
        return afterSteps;
    }

    private void addMatchedSteps(List<String> stepsAsString, List<Step> steps, Map<String, String> namedParameters,
            List<StepCandidate> allCandidates, Outcome outcome) {
        addMatchedSteps(stepsAsString, steps, namedParameters, allCandidates, outcome, DEFAULT_STEP_MONITOR);
    }

    private void addMatchedSteps(List<String> stepsAsString, List<Step> steps, Map<String, String> namedParameters,
            List<StepCandidate> allCandidates, Outcome outcome, StepMonitor stepMonitor) {
        String previousNonAndStep = null;
        for (String stepAsString : stepsAsString) {
            // pending is default step, overridden below
            Step step = StepCreator.createPendingStep(stepAsString, previousNonAndStep);
            List<StepCandidate> prioritisedCandidates = stepFinder.prioritise(stepAsString,
                    new ArrayList<>(allCandidates));
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
                        if ( candidate.isComposite() ){
                            candidate.addComposedSteps(composedSteps, stepAsString, namedParameters, prioritisedCandidates);
                        }
                        if ( outcome != null ){
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
            if ( !(keywords.isAndStep(stepAsString) || keywords.isIgnorableStep(stepAsString)) ){
                previousNonAndStep = stepAsString;
            }
            steps.add(step);
        }
    }
}
