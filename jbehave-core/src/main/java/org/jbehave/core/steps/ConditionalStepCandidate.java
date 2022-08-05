package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jbehave.core.condition.StepConditionMatcher;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.StepCreator.ParametrisedStep;

public class ConditionalStepCandidate extends StepCandidate {

    private final List<Method> methods;
    private final StepConditionMatcher stepConditionMatcher;

    private ConditionalStepCandidate(String patternAsString, int priority, StepType stepType, List<Method> methods,
            Class<?> stepsType, Keywords keywords, StepMatcher stepMatcher, String parameterPrefixString,
            StepCreator stepCreator, StepConditionMatcher stepConditionMatcher) {
        super(patternAsString, priority, stepType, null, stepsType, null, keywords, stepMatcher, parameterPrefixString,
                stepCreator);
        this.methods = methods;
        this.stepConditionMatcher = stepConditionMatcher;
    }

    @Override
    public Method getMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Step createMatchedStep(String stepAsString, Map<String, String> namedParameters, List<Step> composedSteps) {
        Map<Method, ParametrisedStep> parametrisedSteps = methods.stream()
                .collect(Collectors.toMap(Function.identity(), m -> (ParametrisedStep) getStepCreator()
                        .createParametrisedStep(m, stepAsString, stepAsString, namedParameters, composedSteps)));

        return getStepCreator().createConditionalStep(stepConditionMatcher, parametrisedSteps);
    }

    public static StepCandidate from(StepConditionMatcher stepConditionMatcher,
            List<StepCandidate> conditionalCandidates) {
        List<Method> methods = conditionalCandidates.stream().map(StepCandidate::getMethod)
                .collect(Collectors.toList());
        StepCandidate baseCandidate = conditionalCandidates.get(0);

        StepCandidate candidate = new ConditionalStepCandidate(baseCandidate.getPatternAsString(),
                baseCandidate.getPriority(), baseCandidate.getStepType(), methods, baseCandidate.getStepsType(),
                baseCandidate.getKeywords(), baseCandidate.getStepMatcher(),
                baseCandidate.getParameterPrefix(), baseCandidate.getStepCreator(), stepConditionMatcher);
        candidate.useStepMonitor(baseCandidate.getStepMonitor());
        return candidate;
    }
}
