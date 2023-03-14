package org.jbehave.core.expressions;

/**
 * Abstract monitor that reports to output which should be defined in child implementations.
 */
public abstract class PrintingExpressionResolverMonitor implements ExpressionResolverMonitor {

    @Override
    public void onExpressionProcessingError(String stringWithExpressions, RuntimeException error) {
        print("Unable to process expression(s) '%s'", stringWithExpressions);
        printStackTrace(error);
    }

    protected abstract void print(String format, Object... args);

    protected abstract void printStackTrace(Throwable e);
}
