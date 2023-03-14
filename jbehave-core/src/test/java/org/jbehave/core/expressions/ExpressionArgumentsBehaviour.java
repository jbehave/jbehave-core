package org.jbehave.core.expressions;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(MockitoExtension.class)
class ExpressionArgumentsBehaviour {

    static Stream<Arguments> unlimitedExpressionArguments() {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
                arguments("a,b",                                 asList("a", "b")),
                arguments("a, b",                                asList("a", "b")),
                arguments(" a,b ",                               asList("a", "b")),
                arguments(" a, b ",                              asList("a", "b")),
                arguments("a,b,c",                               asList("a", "b", "c")),
                arguments("a, b, c",                             asList("a", "b", "c")),
                arguments("\"\"\"a\"\"\", b",                    asList("a", "b")),
                arguments("\"\"\"a\"\"\", \"\"\"b\"\"\"",        asList("a", "b")),
                arguments("\"\"\"a,b\"\"\", \"\"\"c\"\"\"",      asList("a,b", "c")),
                arguments("\"\"\"a\\,b\"\"\", \"\"\"c\"\"\"",    asList("a\\,b", "c")),
                arguments("\"\"\" a, b \"\"\", \"\"\" c \"\"\"", asList(" a, b ", " c ")),
                arguments("a\\,b,c",                             asList("a,b", "c")),
                arguments("a,b,c\\",                             asList("a", "b", "c\\")),
                arguments("\\a,b,c",                             asList("\\a", "b", "c")),
                arguments(",b,c",                                asList("", "b", "c")),
                arguments("a,b,",                                asList("a", "b", ""))
        );
        // @formatter:on
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("unlimitedExpressionArguments")
    void shouldParseUnlimitedArguments(String commaSeparatedArguments, List<String> parsedArguments) {
        ExpressionArguments argumentsMatcher = new ExpressionArguments(commaSeparatedArguments);
        assertEquals(parsedArguments, argumentsMatcher.getArguments());
    }

    static Stream<Arguments> limitedExpressionArguments() {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
                arguments("a,b",                                 asList("a", "b")),
                arguments("a, b",                                asList("a", "b")),
                arguments(" a,b ",                               asList("a", "b")),
                arguments(" a, b ",                              asList("a", "b")),
                arguments("a,b,c",                               asList("a", "b,c")),
                arguments("a, b, c",                             asList("a", "b, c")),
                arguments("\"\"\"a\"\"\", b",                    asList("a", "b")),
                arguments("\"\"\"a\"\"\", \"\"\"b\"\"\"",        asList("a", "b")),
                arguments("\"\"\"a,b\"\"\", \"\"\"c\"\"\"",      asList("a,b", "c")),
                arguments("\"\"\"a\\,b\"\"\", \"\"\"c\"\"\"",    asList("a\\,b", "c")),
                arguments("\"\"\" a, b \"\"\", \"\"\" c \"\"\"", asList(" a, b ", " c ")),
                arguments("a\\,b,c",                             asList("a,b", "c")),
                arguments("a,b,c\\",                             asList("a", "b,c\\")),
                arguments("\\a,b,c",                             asList("\\a", "b,c")),
                arguments(",b,c",                                asList("", "b,c")),
                arguments("a,b,",                                asList("a", "b,"))
        );
        // @formatter:on
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("limitedExpressionArguments")
    void shouldParseLimitedArguments(String commaSeparatedArguments, List<String> parsedArguments) {
        ExpressionArguments argumentsMatcher = new ExpressionArguments(commaSeparatedArguments, 2);
        assertEquals(parsedArguments, argumentsMatcher.getArguments());
    }
}
