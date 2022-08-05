package org.jbehave.core.condition;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InstanceStepConditionMatcher implements StepConditionMatcher {

    private final Collection<Predicate<Object>> conditions;

    public InstanceStepConditionMatcher(Collection<Predicate<Object>> conditions) {

        conditions.stream()
                  .map(Predicate::getClass)
                  .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                  .forEach((type, count) -> isTrue(count == 1, "Only one instance of %s condition is allowed", type));

        this.conditions = conditions;
    }

    @Override
    public boolean matches(Class<? extends Predicate<Object>> condition, Object value)
            throws StepConditionMatchException {
        return conditions.stream()
                         .filter(c -> c.getClass().equals(condition))
                         .findFirst()
                         .orElseThrow(() -> new StepConditionMatchException(
                                 String.format("Unable to find implementation for the %s condition", condition)))
                         .test(value);
    }
}
