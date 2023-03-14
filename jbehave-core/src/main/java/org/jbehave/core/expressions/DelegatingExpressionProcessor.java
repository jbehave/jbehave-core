package org.jbehave.core.expressions;

import java.util.Collection;
import java.util.Optional;

public class DelegatingExpressionProcessor implements ExpressionProcessor<Object> {

    private final Collection<ExpressionProcessor<?>> delegates;

    public DelegatingExpressionProcessor(Collection<ExpressionProcessor<?>> delegates) {
        this.delegates = delegates;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Object> execute(String expression) {
        return (Optional<Object>) delegates.stream()
                .map(processor -> processor.execute(expression))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(Optional::empty);
    }
}
