package org.jbehave.core.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BiArgExpressionProcessorBehaviour {

    private final BiArgExpressionProcessor<String> processor = new BiArgExpressionProcessor<>("expr", (x, y) -> x + y);

    @ParameterizedTest
    @ValueSource(strings = {
        "expr(x, y)",
        "expr(x,y)",
        "expr( x , y )",
        "expr(x\n, y\n)",
        "expr(x\r\n, y\r\n)",
        "expr(\nx\n,\ny\n)"
    })
    void shouldMatchExpression(String expression) {
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.of("xy"), actual);
    }

    @ParameterizedTest
    @CsvSource({
        "expr(x,y'",
        "expr( x,y",
        "expr",
        "exp",
        "expr(x, y)",
        "''"
    })
    void shouldNotMatchExpression(String expression) {
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.empty(), actual);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "expr(x,y,z) | The expected number of arguments for 'expr' expression is 2, but found 3 arguments: 'x,y,z'",
        "expr(x)     | The expected number of arguments for 'expr' expression is 2, but found 1 argument: 'x'",
        "expr()      | The expected number of arguments for 'expr' expression is 2, but found 0 arguments"
    })
    void shouldFailOnInvalidNumberOfParameters(String expression, String error) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }
}
