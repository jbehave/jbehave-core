package org.jbehave.core.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class InstanceStepConditionMatcherBehaviour {

    @Test
    void shouldMatchCondition() throws StepConditionMatchException {
        TestPrecondition precondition = new TestPrecondition();
        InstanceStepConditionMatcher matcher = new InstanceStepConditionMatcher(
                Collections.singletonList(precondition));
        assertTrue(matcher.matches(TestPrecondition.class, ""));
    }

    @Test
    void shouldFailOnSeveralConditionInstancesOfOneClass() throws StepConditionMatchException {
        List<Predicate<Object>> preconditions = new ArrayList<>(2);
        preconditions.add(new TestPrecondition());
        preconditions.add(new TestPrecondition());
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new InstanceStepConditionMatcher(preconditions));
        assertEquals("Only one instance of class org.jbehave.core.condition.InstanceStepConditionMatcherBehaviour"
                + "$TestPrecondition condition is allowed", thrown.getMessage());
    }

    @Test
    void shouldFailIfConditionInstanceIsNotRegistered() {
        InstanceStepConditionMatcher matcher = new InstanceStepConditionMatcher(Collections.emptyList());
        StepConditionMatchException thrown = assertThrows(StepConditionMatchException.class,
                () -> matcher.matches(TestPrecondition.class, ""));
        assertEquals("Unable to find implementation for the class org.jbehave.core.condition."
                + "InstanceStepConditionMatcherBehaviour$TestPrecondition condition", thrown.getMessage());
    }

    static class TestPrecondition implements Predicate<Object> {

        @Override
        public boolean test(Object t) {
            return true;
        }

    }
}
