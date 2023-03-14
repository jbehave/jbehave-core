package org.jbehave.core.expressions;

/**
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object Pattern</a>
 * implementation of {@link ExpressionResolverMonitor}. Can be extended to override only the methods of interest.
 */
public class NullExpressionResolverMonitor implements ExpressionResolverMonitor {

    @Override
    public void onExpressionProcessingError(String stringWithExpressions, RuntimeException error) {
        // Do nothing by default
    }
}
