package org.jbehave.core.steps;

import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.steps.StepCreator.PendingStep;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

public class PendingStepMethodGenerator {

    private static final String METHOD_SOURCE = "@{0}(\"{1}\")\n@{2}\npublic void {3}()'{'\n  // {4}\n'}'\n";

    private final Keywords keywords;
    private final Map<StepType, String> startingWordsByType;

    public PendingStepMethodGenerator(Keywords keywords) {
        this.keywords = keywords;
        this.startingWordsByType = keywords.startingWordsByType();
    }

    public String generateMethod(PendingStep step) {    
        String stepAsString = step.stepAsString();
        String previousNonAndStepAsString = step.previousNonAndStepAsString();
        StepType stepType = null;
        if (isAndStep(stepAsString)) {
            stepType = findStepType(previousNonAndStepAsString);
        } else {
            stepType = findStepType(stepAsString);
        }
        String stepPattern = stripStartingWord(stepAsString);
        String stepAnnotation = StringUtils.capitalize(stepType.name().toLowerCase());
        String methodName = methodName(stepType, stepPattern);
        String pendingAnnotation = Pending.class.getSimpleName();
        return format(METHOD_SOURCE, stepAnnotation, StringEscapeUtils.escapeJava(stepPattern), pendingAnnotation, methodName, keywords.pending());
    }

    private String methodName(StepType stepType, String stepPattern) {
        String name = stepType.name().toLowerCase() + WordUtils.capitalize(stepPattern);
        for (String remove : asList(" ", "\'", "\"", "\\.", "\\,", "\\;", "\\:", "\\!", "\\|", "<", ">", "\\*")) {
            name = name.replaceAll(remove, "");
        }
        return name;
    }

    private boolean isAndStep(String stepAsString) {
        String andWord = startingWordFor(StepType.AND);
        return stepStartsWithWord(stepAsString, andWord);
    }

    private String stripStartingWord(final String stepAsString) {
        String startingWord = findStartingWord(stepAsString);
        return trimStartingWord(startingWord, stepAsString);
    }

    private StepType findStepType(final String stepAsString) throws StartingWordNotFound {
        for (StepType stepType : startingWordsByType.keySet()) {
            String wordForType = startingWordFor(stepType);
            if (stepStartsWithWord(stepAsString, wordForType)) {
                return stepType;
            }
        }
        throw new StartingWordNotFound(stepAsString, startingWordsByType);
    }

    private String findStartingWord(final String stepAsString) throws StartingWordNotFound {
        for (StepType stepType : startingWordsByType.keySet()) {
            String wordForType = startingWordFor(stepType);
            if (stepStartsWithWord(stepAsString, wordForType)) {
                return wordForType;
            }
        }
        String andWord = startingWordFor(StepType.AND);
        if (stepStartsWithWord(stepAsString, andWord)) {
            return andWord;
        }
        throw new StartingWordNotFound(stepAsString, startingWordsByType);
    }

    private boolean stepStartsWithWord(String step, String word) {
        return step.startsWith(word + " "); // space after qualifies it as word
    }

    private String trimStartingWord(String word, String step) {
        return step.substring(word.length() + 1); // 1 for the space after
    }

    private String startingWordFor(StepType stepType) {
        String startingWord = startingWordsByType.get(stepType);
        if (startingWord == null) {
            throw new StartingWordNotFound(stepType, startingWordsByType);
        }
        return startingWord;
    }

    @SuppressWarnings("serial")
    public static class StartingWordNotFound extends RuntimeException {

        public StartingWordNotFound(String step, Map<StepType, String> startingWordsByType) {
            super("No starting word found for step '" + step + "' amongst '" + startingWordsByType + "'");
        }

        public StartingWordNotFound(StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found of type '" + stepType + "' amongst '" + startingWordsByType + "'");
        }

    }

}