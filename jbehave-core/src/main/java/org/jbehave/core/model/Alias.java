package org.jbehave.core.model;

import java.util.List;

import org.jbehave.core.steps.StepType;

public class Alias {

    private final String stepIdentifier;
    private final StepType type;
    private final List<AliasVariant> variants;

    public Alias(String stepIdentifier, StepType type, List<AliasVariant> variants) {
        this.stepIdentifier = stepIdentifier;
        this.type = type;
        this.variants = variants;
    }

    public String getStepIdentifier() {
        return stepIdentifier;
    }

    public StepType getType() {
        return type;
    }

    public List<AliasVariant> getVariants() {
        return variants;
    }
}
