package org.jbehave.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.steps.StepCreator.StepExecutionType;

public class Step {

    private final StepExecutionType executionType;
    private final String stepAsString;

    public Step(StepExecutionType executionType, String stepAsString) {
        this.executionType = executionType;
        this.stepAsString = stepAsString;
    }

    public StepExecutionType getExecutionType() {
        return executionType;
    }

    public String getStepAsString() {
        return stepAsString;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
