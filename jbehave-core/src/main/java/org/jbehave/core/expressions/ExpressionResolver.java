package org.jbehave.core.expressions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionResolver {

    private static final Pattern GREEDY_EXPRESSION_PATTERN = Pattern.compile("#\\{((?:(?!#\\{|\\$\\{).)*)}",
            Pattern.DOTALL);
    private static final Pattern RELUCTANT_EXPRESSION_PATTERN = Pattern.compile(
            "#\\{((?:(?![#{])[^)](?![()]))*?|(?:(?!#\\{|\\$\\{).)*?\\)|(?:(?!#\\{|\\$\\{).)*?)}",
            Pattern.DOTALL);
    private static final List<Pattern> PATTERNS = Arrays.asList(RELUCTANT_EXPRESSION_PATTERN,
            GREEDY_EXPRESSION_PATTERN);

    private static final String REPLACEMENT_PATTERN = "\\#\\{%s\\}";

    private final Set<ExpressionProcessor<?>> expressionProcessors;
    private final ExpressionResolverMonitor expressionResolverMonitor;

    public ExpressionResolver(Set<ExpressionProcessor<?>> expressionProcessors,
            ExpressionResolverMonitor expressionResolverMonitor) {
        this.expressionProcessors = expressionProcessors;
        this.expressionResolverMonitor = expressionResolverMonitor;
    }

    /**
     * Evaluates expressions including nested ones.
     * <br>
     * Syntax:
     * <br>
     * <code>
     * #{expression(arguments...)}
     * #{expression(arguments..., #{expression(arguments...)})}
     * #{expression(arguments..., #{expression})}
     * </code>
     * <br>
     * Example:
     * <br>
     * <code>
     * #{shiftDate("1942-12-02T01:23:40+04:00", "yyyy-MM-dd'T'HH:mm:ssz", "P43Y4M3W3D")}
     * <br>
     * #{encodeToBase64(#{fromEpochSecond(-523641111)})}
     * </code>
     *
     * @param stringWithExpressions the string with expressions to evaluate
     * @return the resulting string with expression placeholders replaced with expressions evaluation results
     */
    public Object resolveExpressions(boolean dryRun, String stringWithExpressions) {
        if (dryRun) {
            return stringWithExpressions;
        }
        try {
            return resolveExpressions(stringWithExpressions, PATTERNS.iterator());
        } catch (RuntimeException e) {
            expressionResolverMonitor.onExpressionProcessingError(stringWithExpressions, e);
            throw e;
        }
    }

    private Object resolveExpressions(String value, Iterator<Pattern> expressionPatterns) {
        String processedValue = value;
        Matcher expressionMatcher = expressionPatterns.next().matcher(processedValue);
        boolean expressionFound = false;
        while (expressionMatcher.find()) {
            expressionFound = true;
            String expression = expressionMatcher.group(1);
            Object expressionResult = apply(expression);
            if (!(expressionResult instanceof String) && ("#{" + expression + "}").equals(processedValue)) {
                return expressionResult;
            }
            if (!expressionResult.equals(expression)) {
                String regex = String.format(REPLACEMENT_PATTERN, Pattern.quote(expression));
                processedValue = processedValue.replaceFirst(regex,
                        Matcher.quoteReplacement(String.valueOf(expressionResult)));
                expressionFound = false;
                expressionMatcher.reset(processedValue);
            }
        }
        if (expressionFound && expressionPatterns.hasNext()) {
            return resolveExpressions(processedValue, expressionPatterns);
        }
        return processedValue;
    }

    private Object apply(String expression) {
        for (ExpressionProcessor<?> processor : expressionProcessors) {
            Optional<?> optional = processor.execute(expression);
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        return expression;
    }
}
