package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.parsers.StepPatternParser;

public abstract class AbstractCandidateSteps implements CandidateSteps {
    private final Configuration configuration;

    protected AbstractCandidateSteps(Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration configuration() {
        return configuration;
    }

    protected void addCandidatesFromVariants(List<StepCandidate> candidates, Method method, StepType stepType,
            String value, int priority, Class<?> type, InjectableStepsFactory stepsFactory, String[] steps) {
        StepPatternParser stepPatternParser = configuration.stepPatternParser();
        PatternVariantBuilder patternVariantBuilder = new PatternVariantBuilder(value);
        for (String variant : patternVariantBuilder.allVariants()) {
            StepMatcher stepMatcher = stepPatternParser.parseStep(stepType, variant);
            StepCreator stepCreator = createStepCreator(type, stepsFactory, stepMatcher);
            stepCreator.useParanamer(configuration.paranamer());
            StepCandidate candidate = new StepCandidate(variant, priority, stepType, method, type, stepsFactory,
                    configuration.keywords(), stepMatcher, stepPatternParser.getPrefix(), stepCreator, steps,
                    configuration.stepMonitor());
            candidates.add(candidate);
        }
    }

    protected final StepCreator createStepCreator(Class<?> type, InjectableStepsFactory stepsFactory) {
        return createStepCreator(type, stepsFactory, null);
    }

    private StepCreator createStepCreator(Class<?> type, InjectableStepsFactory stepsFactory, StepMatcher stepMatcher) {
        return new StepCreator(type, stepsFactory, configuration.stepsContext(), configuration.parameterConverters(),
                configuration.expressionResolver(), configuration.parameterControls(), stepMatcher,
                configuration.stepMonitor(), configuration.storyControls().dryRun());
    }
}
