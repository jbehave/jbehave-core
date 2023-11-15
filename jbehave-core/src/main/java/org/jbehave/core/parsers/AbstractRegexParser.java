package org.jbehave.core.parsers;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.StepType;

abstract class AbstractRegexParser {

    protected static final String NONE = "";
    private static final String CRLF = "\\r?\\n";

    private final Keywords keywords;

    protected AbstractRegexParser() {
        this(new LocalizedKeywords());
    }

    protected AbstractRegexParser(Keywords keywords) {
        this.keywords = keywords;
    }

    protected Keywords keywords() {
        return keywords;
    }

    protected List<String> splitElements(String text, String keyword) {
        List<String> elements = new ArrayList<>();
        StringBuilder element = new StringBuilder();
        String[] elementsAsText = text.split(keyword);
        for (int i = 0; i < elementsAsText.length; i++) {
            String elementAsText = elementsAsText[i];
            element.append(elementAsText);
            if (isLastLineNotComment(elementAsText)) {
                addNonEmptyElement(element.toString(), elements, keyword);
                element = new StringBuilder();
            } else {
                if (i == elementsAsText.length - 1) {
                    addNonEmptyElement(element.toString(), elements, keyword);
                } else {
                    element.append(keyword);
                }
            }
        }
        return elements;
    }

    private static void addNonEmptyElement(String elementToAdd, List<String> elements, String keyword) {
        if (elementToAdd.trim().length() > 0) {
            elements.add(keyword + "\n" + elementToAdd);
        }
    }

    private boolean isLastLineNotComment(String elementAsText) {
        String[] elementLines = elementAsText.split(CRLF, -1);
        return !elementLines[elementLines.length - 1].startsWith(keywords.ignorable());
    }

    protected String startingWithNL(String text) {
        if (!text.startsWith("\n")) { // always ensure starts with newline
            return "\n" + text;
        }
        return text;
    }

    protected List<String> findSteps(String stepsAsText) {
        Matcher matcher = findingSteps().matcher(stepsAsText);
        List<String> steps = new ArrayList<>();
        int startAt = 0;
        while (matcher.find(startAt)) {
            steps.add(StringUtils.substringAfter(matcher.group(1), "\n"));
            startAt = matcher.start(4);
        }
        return steps;
    }

    // Regex Patterns

    private Pattern findingSteps() {
        String startingWords = concatenateStartingWords();
        return compile(
                "((" + startingWords + ")(.*?))(\\Z|" + startingWords + "|" + CRLF + keywords().examplesTable() + ")",
                DOTALL);
    }

    protected String concatenateStartingWords() {
        List<String> startingWords = Stream.concat(
                keywords().startingWords(stepType -> stepType != StepType.IGNORABLE).map(s -> s + "\\s"),
                keywords().startingWords(stepType -> stepType == StepType.IGNORABLE)
        ).collect(toList());
        return concatenateWithOr(CRLF, startingWords);
    }

    protected String concatenateWithOr(String beforeKeyword, List<String> keywords) {
        StringBuilder builder = new StringBuilder(beforeKeyword).append("(?:");
        for (String keyword : keywords) {
            builder.append(keyword).append('|');
        }
        if (!keywords.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1); // remove last "|"
        }
        return builder.append(')').toString();
    }
}
