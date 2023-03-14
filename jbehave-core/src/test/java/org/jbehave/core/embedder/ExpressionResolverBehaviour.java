/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbehave.core.embedder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.ExpressionProcessor;
import org.jbehave.core.expressions.ExpressionResolver;
import org.jbehave.core.expressions.PrintStreamExpressionResolverMonitor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpressionResolverBehaviour {

    private static final String EXPRESSION_FORMAT = "#{%s}";
    private static final String EXPRESSION_KEYWORD = "target";
    private static final String EXPRESSION_KEYWORD_WITH_SEPARATOR = "tar\nget";
    private static final String EXPRESSION_RESULT = "target result with \\ and $";
    private static final String EXPRESSION_TRIM = "trim";
    private static final String EXPRESSION_CAPITALIZE = "capitalize";
    private static final String EXPRESSION_LOWER_CASE = "toLowerCase";
    private static final String UNSUPPORTED_EXPRESSION_KEYWORD = "unsupported";
    private static final String UNSUPPORTED_EXPRESSION = String.format(EXPRESSION_FORMAT,
            UNSUPPORTED_EXPRESSION_KEYWORD);

    @Mock private ExpressionProcessor<Object> targetProcessor;
    @Mock private ExpressionProcessor<Object> anotherProcessor;

    @ParameterizedTest
    @CsvSource({
        "'target',                    '#{target}',                           %s,              " + EXPRESSION_RESULT,
        "'target',                    '{#{target}}',                         {%s},            " + EXPRESSION_RESULT,
        "'target',                    '{#{target} and #{target}}',           {%1$s and %1$s}, " + EXPRESSION_RESULT,
        "'target(})',                 '#{target(})}',                        %s,              " + EXPRESSION_RESULT,
        "'tar\nget',                  '#{tar\nget}',                         %s,              " + EXPRESSION_RESULT,
        "'expr(value{1})',            '#{expr(#{expr(#{expr(value{1})})})}', %s,              value{1}",
        "'generateDate(-P19Y, yyyy)', '{#{generateDate(-P19Y, yyyy)}\n}',    '{%s\n}',        " + EXPRESSION_RESULT
    })
    void testSupportedExpression(String expressionKeyword, String input, String outputFormat, String outputValue) {
        lenient().when(targetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        when(targetProcessor.execute(expressionKeyword)).thenReturn(Optional.of(outputValue));
        Object actual = expressionResolver.resolveExpressions(false, input);
        String output = String.format(outputFormat, outputValue);
        assertEquals(output, actual);
    }

    @Test
    void testSupportedExpressionNestedExpr() {
        String input = "#{capitalize(#{trim(#{toLowerCase( VIVIDUS )})})}";
        String output = "Vividus";
        ExpressionProcessor<Object> processor = new DelegatingExpressionProcessor(Arrays.asList(
                new SingleArgExpressionProcessor<>(EXPRESSION_TRIM,        StringUtils::trim),
                new SingleArgExpressionProcessor<>(EXPRESSION_LOWER_CASE, StringUtils::lowerCase),
                new SingleArgExpressionProcessor<>(EXPRESSION_CAPITALIZE,  StringUtils::capitalize)
        ));
        ExpressionResolver expressionResolver = new ExpressionResolver(Collections.singleton(processor),
                new PrintStreamExpressionResolverMonitor());
        Object actual = expressionResolver.resolveExpressions(false, input);
        assertEquals(output, actual);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(strings = { EXPRESSION_KEYWORD, "targets" })
    void testNestedExpressionWithoutParams(String nestedExpression) {
        String input = String.format("#{capitalize(#{trim(#{%s})})}", nestedExpression);
        String output = "Quetzalcoatlus";

        SingleArgExpressionProcessor<String> expressionProcessor = mock();
        when(expressionProcessor.execute(nestedExpression)).thenReturn(Optional.of(" quetzalcoatlus "));
        ExpressionProcessor<?> processor = new DelegatingExpressionProcessor(Arrays.asList(
                new SingleArgExpressionProcessor<>(EXPRESSION_TRIM,        StringUtils::trim),
                new SingleArgExpressionProcessor<>(EXPRESSION_CAPITALIZE,  StringUtils::capitalize),
                expressionProcessor
        ));
        ExpressionResolver expressionResolver = new ExpressionResolver(Collections.singleton(processor),
                new PrintStreamExpressionResolverMonitor());
        Object actual = expressionResolver.resolveExpressions(false, input);
        assertEquals(output, actual);
    }

    @Test
    void testUnsupportedExpression() {
        when(anotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(targetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        Object actual = expressionResolver.resolveExpressions(false, UNSUPPORTED_EXPRESSION);
        assertEquals(UNSUPPORTED_EXPRESSION, actual, "Unsupported expression, should leave as is");

        verify(targetProcessor, times(2)).execute(UNSUPPORTED_EXPRESSION_KEYWORD);
        verify(anotherProcessor, times(2)).execute(UNSUPPORTED_EXPRESSION_KEYWORD);
    }

    @ParameterizedTest
    @CsvSource({"${var}", "'#expr'", "{expr}", "value"})
    void testNonExpression(String nonExpression) {
        ExpressionResolver expressionResolver = new ExpressionResolver(Collections.emptySet(),
                new PrintStreamExpressionResolverMonitor());
        Object actual = expressionResolver.resolveExpressions(false, nonExpression);
        assertEquals(nonExpression, actual, "Not expression, should leave as is");

        verify(targetProcessor, never()).execute(nonExpression);
        verify(anotherProcessor, never()).execute(nonExpression);
    }

    @Test
    void testValuesInExampleTable() {
        when(targetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        when(targetProcessor.execute(EXPRESSION_KEYWORD_WITH_SEPARATOR))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        String header = "|value1|value2|value3|value4|\n";
        String inputTable = header + "|#{target}|simple|#{target}|#{tar\nget}|\n|#{target (something inside#$)}|simple"
                + "|#{target}|#{tar\nget}|";
        String expectedTable = header + "|target result with \\ and $|simple|target result with \\ and $|target result"
                + " with \\ and $|\n|target result with \\ and $|simple|target result with \\ and $|target result with"
                + " \\ and $|";
        when(targetProcessor.execute("target (something inside#$)"))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        Object actualTable = expressionResolver.resolveExpressions(false, inputTable);
        assertEquals(expectedTable, actualTable);
        verify(targetProcessor, times(6)).execute(anyString());
    }

    @Test
    void testUnsupportedValuesInExampleTable() {
        when(anotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(targetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        String inputTable = "|value1|value2|value3|\n|#{unsupported}|simple|#{unsupported}|";
        Object actualTable = expressionResolver.resolveExpressions(false, inputTable);
        assertEquals(inputTable, actualTable);
    }

    @Test
    void testMixedValuesInExampleTable() {
        when(anotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(targetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(targetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        when(targetProcessor.execute(EXPRESSION_KEYWORD_WITH_SEPARATOR))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        String anotherExpressionKeyword = "another";
        when(anotherProcessor.execute(anotherExpressionKeyword)).thenReturn(Optional.of("another result"));

        String header = "|value1|value2|value3|value4|value5|\n";
        String inputTable = header + "|#{unsupported}|simple|#{target}|#{tar\nget}|#{another}|";
        String expectedTable = header + "|#{unsupported}|simple|target result with \\ and $|target result with \\ and"
                + " $|another result|";
        Object actualTable = expressionResolver.resolveExpressions(false, inputTable);
        assertEquals(expectedTable, actualTable);
    }

    @Test
    void testMixedExpressionsAndVariablesInExampleTable() {
        when(targetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        String inputTable = "|value1|value2|\n|#{target}|${variable}|";
        String expectedTable = "|value1|value2|\n|target result with \\ and $|${variable}|";
        Object actualTable = expressionResolver.resolveExpressions(false, inputTable);
        assertEquals(expectedTable, actualTable);
    }

    @Test
    void testExpressionProcessingError() {
        String input = "#{exp(any)}";
        RuntimeException exception = new RuntimeException();
        ExpressionProcessor<String> processor = new SingleArgExpressionProcessor<>("exp", value -> {
            throw exception;
        });
        PrintStream printStream = mock();
        ExpressionResolver expressionResolver = new ExpressionResolver(Collections.singleton(processor),
                new PrintStreamExpressionResolverMonitor(printStream));
        RuntimeException actualException = assertThrows(RuntimeException.class,
                () -> expressionResolver.resolveExpressions(false, input));
        assertEquals(exception, actualException);
        verify(printStream).printf("Unable to process expression(s) '%s'%n", input);
    }

    @Test
    void shouldNotProcessExpressionsDuringDryRun() {
        String input = "#{expr(ess)}";
        ExpressionProcessor<?> processor = mock();
        ExpressionResolver expressionResolver = new ExpressionResolver(Collections.singleton(processor),
                new PrintStreamExpressionResolverMonitor());
        assertEquals(input, expressionResolver.resolveExpressions(true, input));
        verifyNoInteractions(processor);
    }

    @Test
    void shouldReturnNotAStringValueForATopLevelExpression() {
        String expression = "#{object(#{string()})}";
        lenient().when(targetProcessor.execute("string()")).thenReturn(Optional.of("result"));
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        Object value = new Object();
        lenient().when(targetProcessor.execute("object(result)")).thenReturn(Optional.of(value));
        assertSame(value, expressionResolver.resolveExpressions(false, expression));
    }

    @ParameterizedTest
    @CsvSource({
        "'#{string(#{integer()})}', '#{string(42)}'",
        "'24 + #{integer()}',       '24 + 42'",
        "'#{integer()} + 24',       '42 + 24'"
    })
    void shouldConvertNotAStringValueToAStringForNotTopLevelExpression(String expression, String expected) {
        lenient().when(targetProcessor.execute("integer()")).thenReturn(Optional.of(42));
        ExpressionResolver expressionResolver =
                new ExpressionResolver(new LinkedHashSet<>(Arrays.asList(targetProcessor, anotherProcessor)),
                        new PrintStreamExpressionResolverMonitor());
        assertEquals(expected, expressionResolver.resolveExpressions(false, expression));
    }
}
