package org.jbehave.core.condition;

import java.lang.reflect.Constructor;
import java.util.function.Predicate;

public class ReflectionBasedStepConditionMatcher implements StepConditionMatcher {

    @Override
    public boolean matches(Class<? extends Predicate<Object>> type, Object value) throws StepConditionMatchException {
        try {
            Constructor<? extends Predicate<Object>> constructor = type.getConstructor();
            Predicate<Object> instance = constructor.newInstance();
            return instance.test(value);
        } catch (NoSuchMethodException e) {
            throw new StepConditionMatchException(
                    "Condition implementation class must have public no-args constructor");
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new StepConditionMatchException(e);
        }
    }

}
