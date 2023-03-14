package org.jbehave.core.expressions;

import java.util.List;
import java.util.function.Function;

public class RelaxedMultiArgExpressionProcessor<T> extends MultiArgExpressionProcessor<T> {

    public RelaxedMultiArgExpressionProcessor(String expressionName, int argsLimit,
            Function<List<String>, T> transformer) {
        super(expressionName, argsLimit, argumentsAsString -> new ExpressionArguments(argumentsAsString, argsLimit),
                transformer);
    }

    public RelaxedMultiArgExpressionProcessor(String expressionName, int minArgNumber, int argsLimit,
            Function<List<String>, T> transformer) {
        super(expressionName, minArgNumber, argsLimit,
                argumentsAsString -> new ExpressionArguments(argumentsAsString, argsLimit), transformer);
    }
}
