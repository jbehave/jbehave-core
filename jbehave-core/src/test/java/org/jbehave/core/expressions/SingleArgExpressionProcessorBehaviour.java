package org.jbehave.core.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SingleArgExpressionProcessorBehaviour {

    private final SingleArgExpressionProcessor<String> processor = new SingleArgExpressionProcessor<>("expression",
            Function.identity());

    @ParameterizedTest
    @CsvSource({
        "'expression(x)',     'x'",
        "'expression()',      ''",
        "'expression( x )',   ' x '",
        "'expression(x\n)',   'x\n'",
        "'expression(x\r\n)', 'x\r\n'",
        "'expression(\nx\n)', '\nx\n'"
    })
    void shouldMatchExpression(String expression, String expected) {
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.of(expected), actual);
    }

    @ParameterizedTest
    @CsvSource({
        "expression(x'",
        "expression( x",
        "expression",
        "expressio",
        "expressio(x)",
        "''"
    })
    void shouldNotMatchExpression(String expression) {
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.empty(), actual);
    }
}
