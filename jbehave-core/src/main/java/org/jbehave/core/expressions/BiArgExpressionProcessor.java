package org.jbehave.core.expressions;

import java.util.function.BiFunction;

public class BiArgExpressionProcessor<T> extends MultiArgExpressionProcessor<T> {

    public BiArgExpressionProcessor(String expressionName, BiFunction<String, String, T> evaluator) {
        super(expressionName, 2, args -> evaluator.apply(args.get(0), args.get(1)));
    }
}
