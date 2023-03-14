package org.jbehave.core.expressions;

public interface ExpressionResolverMonitor {

    void onExpressionProcessingError(String stringWithExpressions, RuntimeException error);
}
