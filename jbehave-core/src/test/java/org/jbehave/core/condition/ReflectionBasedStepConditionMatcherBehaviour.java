package org.jbehave.core.condition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class ReflectionBasedStepConditionMatcherBehaviour {

    private final StepConditionMatcher matcher = new ReflectionBasedStepConditionMatcher();

    @Test
    void shouldReturnExpectedValue() throws StepConditionMatchException {
        assertThat(matcher.matches(TestCondition.class, false), is(false));
        assertThat(matcher.matches(TestCondition.class, true), is(true));
    }

    @Test
    void shouldFailIfConditionHasNoArgsConstructor() {
        StepConditionMatchException thrown = assertThrows(StepConditionMatchException.class,
                () -> matcher.matches(ErrorCondition.class, false));
        assertEquals("Condition implementation class must have public no-args constructor", thrown.getMessage());
    }

    public static class TestCondition implements Predicate<Object> {
        @Override
        public boolean test(Object t) {
            return t.equals(Boolean.TRUE);
        }
    }

    public static class ErrorCondition implements Predicate<Object> {
        public ErrorCondition(String arg) {
        }

        @Override
        public boolean test(Object t) {
            return t.equals(Boolean.TRUE);
        }
    }
}
