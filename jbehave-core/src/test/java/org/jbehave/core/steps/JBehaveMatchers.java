package org.jbehave.core.steps;

import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;

import org.jbehave.core.model.Step;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.mockito.ArgumentMatcher;

public final class JBehaveMatchers {
    private JBehaveMatchers() {
    }

    public static Step step(StepExecutionType type, String name) {
        ArgumentMatcher<Step> matcher = arg -> name.equals(arg.getStepAsString())
                && type.equals(arg.getExecutionType());
        mockingProgress().getArgumentMatcherStorage().reportMatcher(matcher);
        return null;
    }
}

