package org.jbehave.core.condition;

import java.util.function.Predicate;

public interface StepConditionMatcher {

    /**
     * Checks whether the <b>condition</b> matches the input <b>value</b>
     * @param condition a class with the implementation of the condition
     * @param value the argument to be matched against the condition
     * @return the condition evaluation result
     * @throws StepConditionMatchException if any error during match process occur
     */
    boolean matches(Class<? extends Predicate<Object>> condition, Object value) throws StepConditionMatchException;

}
