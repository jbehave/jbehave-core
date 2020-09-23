package org.jbehave.core.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

abstract class StepsContainer {

    private final List<String> steps;

    StepsContainer(List<String> steps) {
        this.steps = steps;
    }

    public List<String> getSteps() {
        return getSteps(true);
    }

    public List<String> getSteps(boolean trim) {
        return trim ? trim(steps) : steps;
    }

    private List<String> trim(List<String> steps){
        return steps.stream()
                .map(String::trim)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
