package org.jbehave.core.expressions;

import java.util.Optional;

public interface ExpressionProcessor<T> {
    Optional<T> execute(String expression);
}
