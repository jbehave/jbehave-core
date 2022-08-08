package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbehave.core.condition.StepConditionMatcher;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.StepCreator.ParametrisedStep;

public class ConditionalStepCandidate extends StepCandidate {

    private final Map<Method, StepCreator> stepCreators;
    private final StepConditionMatcher stepConditionMatcher;

    private ConditionalStepCandidate(String patternAsString, int priority, StepType stepType,
            Map<Method, StepCreator> stepCreators, Keywords keywords, StepMatcher stepMatcher,
            String parameterPrefixString, StepCreator stepCreator, StepConditionMatcher stepConditionMatcher) {
        super(patternAsString, priority, stepType, null, null, null, keywords, stepMatcher, parameterPrefixString,
                stepCreator);
        this.stepCreators = stepCreators;
        this.stepConditionMatcher = stepConditionMatcher;
    }

    @Override
    public Method getMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Step createMatchedStep(String stepAsString, Map<String, String> namedParameters, List<Step> composedSteps) {
        Map<Method, ParametrisedStep> parametrisedSteps = stepCreators.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> (ParametrisedStep) e.getValue().createParametrisedStep(e.getKey(), stepAsString,
                                stepAsString, namedParameters, composedSteps)));

        return getStepCreator().createConditionalStep(stepConditionMatcher, parametrisedSteps);
    }

    public static StepCandidate from(StepConditionMatcher stepConditionMatcher,
            List<StepCandidate> conditionalCandidates) {
        Map<Method, StepCreator> stepCreators = conditionalCandidates.stream()
                .collect(Collectors.toMap(StepCandidate::getMethod, StepCandidate::getStepCreator));

        StepCandidate baseCandidate = conditionalCandidates.get(0);

        StepCandidate candidate = new ConditionalStepCandidate(baseCandidate.getPatternAsString(),
                baseCandidate.getPriority(), baseCandidate.getStepType(), stepCreators,
                baseCandidate.getKeywords(), baseCandidate.getStepMatcher(),
                baseCandidate.getParameterPrefix(), baseCandidate.getStepCreator(), stepConditionMatcher);
        candidate.useStepMonitor(baseCandidate.getStepMonitor());
        return candidate;
    }
}
