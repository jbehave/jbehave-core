package org.jbehave.core.expressions;

import java.util.function.Function;

public class SingleArgExpressionProcessor<T> extends MultiArgExpressionProcessor<T> {

    public SingleArgExpressionProcessor(String functionName, Function<String, T> evaluator) {
        super(functionName, 1, args -> evaluator.apply(args.get(0)));
    }
}
