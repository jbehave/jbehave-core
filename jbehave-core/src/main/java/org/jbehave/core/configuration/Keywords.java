package org.jbehave.core.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.steps.StepType;

import static java.util.Arrays.asList;

/**
 * Provides the keywords which allow parsers to find steps in stories and match
 * those steps with candidates through the annotations. It provides the starting
 * words (Given, When, Then And, "!--") using in parsing, as well as providing
 * keywords used in reporting.
 */
public class Keywords {

    public static final String META = "Meta";
    public static final String META_PROPERTY = "MetaProperty";
    public static final String NARRATIVE = "Narrative";
    public static final String IN_ORDER_TO = "InOrderTo";
    public static final String AS_A = "AsA";
    public static final String I_WANT_TO = "IWantTo";
    public static final String SCENARIO = "Scenario";
    public static final String GIVEN_STORIES = "GivenStories";
    public static final String EXAMPLES_TABLE = "ExamplesTable";
    public static final String EXAMPLES_TABLE_ROW = "ExamplesTableRow";
    public static final String EXAMPLES_TABLE_HEADER_SEPARATOR = "ExamplesTableHeaderSeparator";
    public static final String EXAMPLES_TABLE_VALUE_SEPARATOR = "ExamplesTableValueSeparator";
    public static final String EXAMPLES_TABLE_IGNORABLE_SEPARATOR = "ExamplesTableIgnorableSeparator";
    public static final String GIVEN = "Given";
    public static final String WHEN = "When";
    public static final String THEN = "Then";
    public static final String AND = "And";
    public static final String IGNORABLE = "Ignorable";
    public static final String PENDING = "Pending";
    public static final String NOT_PERFORMED = "NotPerformed";
    public static final String FAILED = "Failed";
    public static final String DRY_RUN = "DryRun";

    public static final List<String> KEYWORDS = asList(META, META_PROPERTY, NARRATIVE, IN_ORDER_TO, AS_A, I_WANT_TO, SCENARIO,
            GIVEN_STORIES, EXAMPLES_TABLE, EXAMPLES_TABLE_ROW, EXAMPLES_TABLE_HEADER_SEPARATOR,
            EXAMPLES_TABLE_VALUE_SEPARATOR, EXAMPLES_TABLE_IGNORABLE_SEPARATOR, GIVEN, WHEN, THEN, AND, IGNORABLE,
            PENDING, NOT_PERFORMED, FAILED, DRY_RUN);

    private final String meta;
    private final String metaProperty;    
    private final String narrative;
    private final String inOrderTo;
    private final String asA;
    private final String iWantTo;
    private final String scenario;
    private final String givenStories;
    private final String examplesTable;
    private final String examplesTableRow;
    private final String examplesTableHeaderSeparator;
    private final String examplesTableValueSeparator;
    private final String examplesTableIgnorableSeparator;
    private final String given;
    private final String when;
    private final String then;
    private final String and;
    private final String ignorable;
    private final String pending;
    private final String notPerformed;
    private final String failed;
    private final String dryRun;
    private final Map<StepType, String> startingWordsByType = new HashMap<StepType, String>();

    public static Map<String, String> defaultKeywords() {
        Map<String, String> keywords = new HashMap<String, String>();
        keywords.put(META, "Meta:");
        keywords.put(META_PROPERTY, "@");
        keywords.put(NARRATIVE, "Narrative:");
        keywords.put(IN_ORDER_TO, "In order to:");
        keywords.put(AS_A, "As a:");
        keywords.put(I_WANT_TO, "I want to:");
        keywords.put(SCENARIO, "Scenario:");
        keywords.put(GIVEN_STORIES, "GivenStories:");
        keywords.put(EXAMPLES_TABLE, "Examples:");
        keywords.put(EXAMPLES_TABLE_ROW, "Example:");
        keywords.put(EXAMPLES_TABLE_HEADER_SEPARATOR, "|");
        keywords.put(EXAMPLES_TABLE_VALUE_SEPARATOR, "|");
        keywords.put(EXAMPLES_TABLE_IGNORABLE_SEPARATOR, "|--");
        keywords.put(GIVEN, "Given");
        keywords.put(WHEN, "When");
        keywords.put(THEN, "Then");
        keywords.put(AND, "And");
        keywords.put(IGNORABLE, "!--");
        keywords.put(PENDING, "PENDING");
        keywords.put(NOT_PERFORMED, "NOT PERFORMED");
        keywords.put(FAILED, "FAILED");
        keywords.put(DRY_RUN, "DRY RUN");
        return keywords;
    }

    /**
     * Creates Keywords with default values {@link #defaultKeywords()}
     */
    public Keywords() {
        this(defaultKeywords());
    }

    /**
     * Creates Keywords with provided keywords Map and Encoding
     * 
     * @param keywords
     *            the Map of keywords indexed by their name
     */
    public Keywords(Map<String, String> keywords) {
        this.meta = keyword(META, keywords);
        this.metaProperty = keyword(META_PROPERTY, keywords);
        this.narrative = keyword(NARRATIVE, keywords);
        this.inOrderTo = keyword(IN_ORDER_TO, keywords);
        this.asA = keyword(AS_A, keywords);
        this.iWantTo = keyword(I_WANT_TO, keywords);
        this.scenario = keyword(SCENARIO, keywords);
        this.givenStories = keyword(GIVEN_STORIES, keywords);
        this.examplesTable = keyword(EXAMPLES_TABLE, keywords);
        this.examplesTableRow = keyword(EXAMPLES_TABLE_ROW, keywords);
        this.examplesTableHeaderSeparator = keyword(EXAMPLES_TABLE_HEADER_SEPARATOR, keywords);
        this.examplesTableValueSeparator = keyword(EXAMPLES_TABLE_VALUE_SEPARATOR, keywords);
        this.examplesTableIgnorableSeparator = keyword(EXAMPLES_TABLE_IGNORABLE_SEPARATOR, keywords);
        this.given = keyword(GIVEN, keywords);
        this.when = keyword(WHEN, keywords);
        this.then = keyword(THEN, keywords);
        this.and = keyword(AND, keywords);
        this.ignorable = keyword(IGNORABLE, keywords);
        this.pending = keyword(PENDING, keywords);
        this.notPerformed = keyword(NOT_PERFORMED, keywords);
        this.failed = keyword(FAILED, keywords);
        this.dryRun = keyword(DRY_RUN, keywords);
        
        startingWordsByType.put(StepType.GIVEN, given());
        startingWordsByType.put(StepType.WHEN, when());
        startingWordsByType.put(StepType.THEN, then());
        startingWordsByType.put(StepType.AND, and());
        startingWordsByType.put(StepType.IGNORABLE, ignorable());

    }

    private String keyword(String name, Map<String, String> keywords) {
        String keyword = keywords.get(name);
        if (keyword == null) {
            throw new KeywordNotFound(name, keywords);
        }
        return keyword;
    }
    

    public String meta() {
        return meta;
    }

    public String metaProperty() {
        return metaProperty;
    }

    public String narrative() {
        return narrative;
    }

    public String inOrderTo() {
        return inOrderTo;
    }

    public String asA() {
        return asA;
    }

    public String iWantTo() {
        return iWantTo;
    }

    public String scenario() {
        return scenario;
    }

    public String givenStories() {
        return givenStories;
    }

    public String examplesTable() {
        return examplesTable;
    }

    public String examplesTableRow() {
        return examplesTableRow;
    }

    public String examplesTableHeaderSeparator() {
        return examplesTableHeaderSeparator;
    }

    public String examplesTableValueSeparator() {
        return examplesTableValueSeparator;
    }

    public String examplesTableIgnorableSeparator() {
        return examplesTableIgnorableSeparator;
    }

    public String given() {
        return given;
    }

    public String when() {
        return when;
    }

    public String then() {
        return then;
    }

    public String and() {
        return and;
    }

    public String ignorable() {
        return ignorable;
    }

    public String pending() {
        return pending;
    }

    public String notPerformed() {
        return notPerformed;
    }

    public String failed() {
        return failed;
    }

    public String dryRun() {
        return dryRun;
    }

    public String[] startingWords() {
        Collection<String> words = startingWordsByType().values();
        return words.toArray(new String[words.size()]);
    }

    public Map<StepType, String> startingWordsByType() {
        return startingWordsByType;
    }

    public boolean isAndStep(String stepAsString) {
        String andWord = startingWordFor(StepType.AND);
        return stepStartsWithWord(stepAsString, andWord);
    }
    
    public String stepWithoutStartingWord(String stepAsString, StepType stepType) {
        String startingWord = startingWord(stepAsString, stepType);
        return stepAsString.substring(startingWord.length() + 1); // 1 for the space after
    }

    public String startingWord(String stepAsString, StepType stepType) throws StartingWordNotFound {
        String wordForType = startingWordFor(stepType);
        if (stepStartsWithWord(stepAsString, wordForType)) {
            return wordForType;
        }
        String andWord = startingWordFor(StepType.AND);
        if (stepStartsWithWord(stepAsString, andWord)) {
            return andWord;
        }
        throw new StartingWordNotFound(stepAsString, stepType, startingWordsByType);
    }

    public String startingWord(String stepAsString) throws StartingWordNotFound {
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

    public StepType stepTypeFor(String stepAsString) throws StartingWordNotFound {
        for (StepType stepType : startingWordsByType.keySet()) {
            String wordForType = startingWordFor(stepType);
            if (stepStartsWithWord(stepAsString, wordForType)) {
                return stepType;
            }
        }
        throw new StartingWordNotFound(stepAsString, startingWordsByType);
    }


    public boolean stepStartsWithWord(String step, String word) {
        return step.startsWith(word + " "); // space after qualifies it as word
    }

    public String startingWordFor(StepType stepType) {
        String startingWord = startingWordsByType.get(stepType);
        if (startingWord == null) {
            throw new StartingWordNotFound(stepType, startingWordsByType);
        }
        return startingWord;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings("serial")
    public static class KeywordNotFound extends RuntimeException {

        public KeywordNotFound(String name, Map<String, String> keywords) {
            super("Keyword " + name + " not found amongst " + keywords);
        }

    }
    
    @SuppressWarnings("serial")
    public static class StartingWordNotFound extends RuntimeException {

        public StartingWordNotFound(String step, StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found for step '" + step + "' of type '" + stepType + "' amongst '"
                    + startingWordsByType + "'");
        }

        public StartingWordNotFound(StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found of type '" + stepType + "' amongst '" + startingWordsByType + "'");
        }

        public StartingWordNotFound(String stepAsString, Map<StepType, String> startingWordsByType) {
            // TODO Auto-generated constructor stub
        }

    }



}
