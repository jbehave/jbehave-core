package org.jbehave.core.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MultiArgExpressionProcessorBehaviour {

    private static final String EXPRESSION_NAME = "expr";
    private static final Function<List<String>, String> EVALUATOR = args -> String.join("+", args);

    @ParameterizedTest
    @CsvSource({
        "'expr(x, y)',         x+y",
        "'expr(x,y)',          x+y",
        "'expr( x , y )',      x+y",
        "'expr(x\n, y\n)',     x+y",
        "'expr(x\r\n, y\r\n)', x+y",
        "'expr(\nx\n,\ny\n)',  x+y"
    })
    void shouldMatchExpressionExactNumberOfArguments(String expression, String expected) {
        MultiArgExpressionProcessor<String> processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2,
                EVALUATOR);
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.of(expected), actual);
    }

    @ParameterizedTest
    @CsvSource({
        "'expr(x, y, z)',      'x+y+z'",
        "'expr(x,y,z)',        'x+y+z'",
        "'expr(x, y)',         x+y",
        "'expr(x,y)',          x+y"
    })
    void shouldMatchExpressionWithDifferentNumberOfArguments(String expression, String expected) {
        MultiArgExpressionProcessor<String> processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2, 3,
                EVALUATOR);
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.of(expected), actual);
    }

    @ParameterizedTest
    @CsvSource({
        "expr(x,y'",
        "expr( x,y",
        EXPRESSION_NAME,
        "exp",
        "expr(x, y)",
        "''"
    })
    void shouldNotMatchExpression(String expression) {
        MultiArgExpressionProcessor<String> processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2,
                EVALUATOR);
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.empty(), actual);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "expr(x)       | The expected number of arguments for 'expr' expression is 2, but found 1 argument: 'x'",
        "expr()        | The expected number of arguments for 'expr' expression is 2, but found 0 arguments",
        "expr(x, y, z) | The expected number of arguments for 'expr' expression is 2, but found 3 arguments: 'x, y, z'",
        "expr(x,y,z)   | The expected number of arguments for 'expr' expression is 2, but found 3 arguments: 'x,y,z'"
    })
    void shouldFailOnInvalidNumberOfParameters(String expression, String error) {
        MultiArgExpressionProcessor<String> processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2,
                EVALUATOR);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // CHECKSTYLE:OFF
        // @formatter:off
        "expr()        | The expected number of arguments for 'expr' expression is from 1 to 3, but found 0 arguments",
        "expr(x,y,z,a) | The expected number of arguments for 'expr' expression is from 1 to 3, but found 4 arguments: 'x,y,z,a'",
        // @formatter:on
        // CHECKSTYLE:ON
    })
    void shouldFailOnInvalidNumberOfParametersForExpressionAcceptingRange(String expression, String error) {
        MultiArgExpressionProcessor<String> processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 1, 3,
                EVALUATOR);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }
}
