package org.jbehave.core.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.steps.StepType;

import static java.util.Arrays.asList;

/**
 * Provides the keywords which allow parsers to find steps in stories and match
 * those steps with candidates through the annotations. It provides the starting
 * words (Given, When, Then And, "!--") using in parsing, as well as providing
 * keywords used in reporting.
 */
public class Keywords {

    private static final String SYNONYM_SEPARATOR = "\\|";
    
    public static final String META = "Meta";
    public static final String META_PROPERTY = "MetaProperty";
    public static final String NARRATIVE = "Narrative";
    public static final String IN_ORDER_TO = "InOrderTo";
    public static final String AS_A = "AsA";
    public static final String I_WANT_TO = "IWantTo";
    public static final String SO_THAT = "SoThat";
    public static final String SCENARIO = "Scenario";
    public static final String GIVEN_STORIES = "GivenStories";
    public static final String LIFECYCLE = "Lifecycle";
    public static final String BEFORE = "Before";
    public static final String AFTER = "After";
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
    public static final String STORY_CANCELLED = "StoryCancelled";
    public static final String DURATION = "Duration";
    public static final String SCOPE = "Scope";
    public static final String SCOPE_SCENARIO = "ScopeScenario";
    public static final String SCOPE_STORY = "ScopeStory";
    public static final String OUTCOME = "Outcome";
    public static final String OUTCOME_ANY = "OutcomeAny";
    public static final String OUTCOME_SUCCESS = "OutcomeSuccess";
    public static final String OUTCOME_FAILURE = "OutcomeFailure";
    public static final String OUTCOME_DESCRIPTION = "OutcomeDescription";
    public static final String OUTCOME_VALUE = "OutcomeValue";
    public static final String OUTCOME_MATCHER = "OutcomeMatcher";
    public static final String OUTCOME_VERIFIED = "OutcomeVerified";
    public static final String META_FILTER = "MetaFilter";
    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final List<String> KEYWORDS = asList(META, META_PROPERTY, NARRATIVE, IN_ORDER_TO, AS_A, I_WANT_TO, SO_THAT,
            SCENARIO, GIVEN_STORIES, LIFECYCLE, BEFORE, AFTER, EXAMPLES_TABLE, EXAMPLES_TABLE_ROW, EXAMPLES_TABLE_HEADER_SEPARATOR,
            EXAMPLES_TABLE_VALUE_SEPARATOR, EXAMPLES_TABLE_IGNORABLE_SEPARATOR, GIVEN, WHEN, THEN, AND, IGNORABLE,
            PENDING, NOT_PERFORMED, FAILED, DRY_RUN, STORY_CANCELLED, DURATION, SCOPE, SCOPE_SCENARIO, SCOPE_STORY, OUTCOME, OUTCOME_ANY, OUTCOME_SUCCESS, OUTCOME_FAILURE,
            OUTCOME_DESCRIPTION, OUTCOME_VALUE, OUTCOME_MATCHER, OUTCOME_VERIFIED, META_FILTER, YES, NO);


    private final String meta;
    private final String metaProperty;
    private final String narrative;
    private final String inOrderTo;
    private final String asA;
    private final String iWantTo;
    private final String soThat;
    private final String scenario;
    private final String givenStories;
    private final String lifecycle;
    private final String before;
    private final String after;
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
    private final String storyCancelled;
    private final String duration;
    private final String scope;
    private final String scopeScenario;
    private final String scopeStory;
    private final String outcome;
    private final String outcomeAny;
    private final String outcomeSuccess;
    private final String outcomeFailure;
    private final String outcomeDescription;
    private final String outcomeValue;
    private final String outcomeMatcher;
    private final String outcomeVerified;
    private final String metaFilter;
    private final String yes;
    private final String no;
    private final Map<StepType, String> startingWordsByType = new HashMap<>();


    public static Map<String, String> defaultKeywords() {
        Map<String, String> keywords = new HashMap<>();
        keywords.put(META, "Meta:");
        keywords.put(META_PROPERTY, "@");
        keywords.put(NARRATIVE, "Narrative:");
        keywords.put(IN_ORDER_TO, "In order to");
        keywords.put(AS_A, "As a");
        keywords.put(I_WANT_TO, "I want to");
        keywords.put(SO_THAT, "So that");
        keywords.put(SCENARIO, "Scenario:");
        keywords.put(GIVEN_STORIES, "GivenStories:");
        keywords.put(LIFECYCLE, "Lifecycle:");
        keywords.put(BEFORE, "Before:");
        keywords.put(AFTER, "After:");
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
        keywords.put(STORY_CANCELLED, "STORY CANCELLED");
        keywords.put(DURATION, "DURATION");
        keywords.put(SCOPE, "Scope:");
        keywords.put(SCOPE_SCENARIO, "SCENARIO");
        keywords.put(SCOPE_STORY, "STORY");
        keywords.put(OUTCOME, "Outcome:");
        keywords.put(OUTCOME_ANY, "ANY");
        keywords.put(OUTCOME_SUCCESS, "SUCCESS");
        keywords.put(OUTCOME_FAILURE, "FAILURE");
        keywords.put(OUTCOME_DESCRIPTION, "DESCRIPTION");
        keywords.put(OUTCOME_MATCHER, "MATCHER");
        keywords.put(OUTCOME_VALUE, "VALUE");
        keywords.put(OUTCOME_VERIFIED, "VERIFIED");
        keywords.put(META_FILTER, "MetaFilter:");
        keywords.put(YES, "Yes");
        keywords.put(NO, "No");
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
     * @param keywords the Map of keywords indexed by their name
     */
    public Keywords(Map<String, String> keywords) {
        this.meta = keyword(META, keywords);
        this.metaProperty = keyword(META_PROPERTY, keywords);
        this.narrative = keyword(NARRATIVE, keywords);
        this.inOrderTo = keyword(IN_ORDER_TO, keywords);
        this.asA = keyword(AS_A, keywords);
        this.iWantTo = keyword(I_WANT_TO, keywords);
        this.soThat = keyword(SO_THAT, keywords);
        this.scenario = keyword(SCENARIO, keywords);
        this.givenStories = keyword(GIVEN_STORIES, keywords);
        this.lifecycle = keyword(LIFECYCLE, keywords);
        this.before = keyword(BEFORE, keywords);
        this.after = keyword(AFTER, keywords);
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
        this.storyCancelled = keyword(STORY_CANCELLED, keywords);
        this.duration = keyword(DURATION, keywords);
        this.scope = keyword(SCOPE, keywords);
        this.scopeScenario = keyword(SCOPE_SCENARIO, keywords);
        this.scopeStory = keyword(SCOPE_STORY, keywords);
        this.outcome = keyword(OUTCOME, keywords);
        this.outcomeAny = keyword(OUTCOME_ANY, keywords);
        this.outcomeSuccess = keyword(OUTCOME_SUCCESS, keywords);
        this.outcomeFailure = keyword(OUTCOME_FAILURE, keywords);
        this.outcomeDescription = keyword(OUTCOME_DESCRIPTION, keywords);
        this.outcomeMatcher = keyword(OUTCOME_MATCHER, keywords);
        this.outcomeValue = keyword(OUTCOME_VALUE, keywords);
        this.outcomeVerified = keyword(OUTCOME_VERIFIED, keywords);
        this.metaFilter = keyword(META_FILTER, keywords);
        this.yes = keyword(YES, keywords);
        this.no = keyword(NO, keywords);

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

    public String soThat() {
        return soThat;
    }

    public String scenario() {
        return scenario;
    }

    public String givenStories() {
        return givenStories;
    }

    public String lifecycle() {
        return lifecycle;
    }

    public String before() {
        return before;
    }

    public String after() {
        return after;
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

    public String storyCancelled() {
        return storyCancelled;
    }

    public String duration() {
        return duration;
    }

    public String scope() { return scope; }

    public String scopeScenario() { return scopeScenario; }

    public String scopeStory() { return scopeStory; }

    public String outcome() {
        return outcome;
    }

    public String outcomeAny(){
        return outcomeAny;
    }

    public String outcomeSuccess(){
        return outcomeSuccess;
    }

    public String outcomeFailure(){
        return outcomeFailure;
    }

    public List<String> outcomeFields() {
        return asList(outcomeDescription, outcomeValue, outcomeMatcher, outcomeVerified);
    }

    public String metaFilter() {
        return metaFilter;
    }

    public String yes() {
        return yes;
    }

    public String no() {
        return no;
    }

    public String[] synonymsOf(String word) {
        return word.split(SYNONYM_SEPARATOR);
    }

    public String[] startingWords() {
        List<String> words = new ArrayList<>();
        for (String word : startingWordsByType().values()) {
            words.addAll(asList(synonymsOf(word)));
        }
        return words.toArray(new String[words.size()]);
    }

    public Map<StepType, String> startingWordsByType() {
        return startingWordsByType;
    }

    private boolean ofStepType(String stepAsString, StepType stepType) {
        boolean isType = false;
        for (String word : startingWordsFor(stepType)) {
            isType = stepStartsWithWord(stepAsString, word);
            if (isType)
                break;
        }
        return isType;
    }

    public boolean isAndStep(String stepAsString) {
        return ofStepType(stepAsString, StepType.AND);
    }

    public boolean isIgnorableStep(String stepAsString) {
        return ofStepType(stepAsString, StepType.IGNORABLE);
    }

    public String stepWithoutStartingWord(String stepAsString, StepType stepType) {
        String startingWord = startingWord(stepAsString, stepType);
        return stepAsString.substring(startingWord.length() + 1); // 1 for the
                                                                  // space after
    }

    public String startingWord(String stepAsString, StepType stepType) throws StartingWordNotFound {
        for (String wordForType : startingWordsFor(stepType)) {
            if (stepStartsWithWord(stepAsString, wordForType)) {
                return wordForType;
            }
        }
        for (String andWord : startingWordsFor(StepType.AND)) {
            if (stepStartsWithWord(stepAsString, andWord)) {
                return andWord;
            }
        }
        throw new StartingWordNotFound(stepAsString, stepType, startingWordsByType);
    }

    public String startingWord(String stepAsString) throws StartingWordNotFound {
        for (StepType stepType : startingWordsByType.keySet()) {
            for (String wordForType : startingWordsFor(stepType)) {
                if (stepStartsWithWord(stepAsString, wordForType)) {
                    return wordForType;
                }
            }
        }
        throw new StartingWordNotFound(stepAsString, startingWordsByType);
    }

    public StepType stepTypeFor(String stepAsString) throws StartingWordNotFound {
        for (StepType stepType : startingWordsByType.keySet()) {
            for (String wordForType : startingWordsFor(stepType)) {
                if (stepStartsWithWord(stepAsString, wordForType)) {
                    return stepType;
                }
            }
        }
        throw new StartingWordNotFound(stepAsString, startingWordsByType);
    }

    public boolean stepStartsWithWord(String step, String word) {
        return stepStartsWithWords(step, word);
    }

    public boolean stepStartsWithWords(String step, String... words) {
        char separator = ' '; // space after qualifies it as word
        String start = StringUtils.join(words, separator) + separator;
        return step.startsWith(start);
    }

    public String startingWordFor(StepType stepType) {
        String startingWord = startingWordsByType.get(stepType);
        if (startingWord == null) {
            throw new StartingWordNotFound(stepType, startingWordsByType);
        }
        return startingWord;
    }

    public String[] startingWordsFor(StepType stepType) {
        return synonymsOf(startingWordFor(stepType));
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

        public StartingWordNotFound(String step, Map<StepType, String> startingWordsByType) {
            super("No starting word found for step '" + step + "' amongst '" + startingWordsByType + "'");
        }

        public StartingWordNotFound(StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found of type '" + stepType + "' amongst '" + startingWordsByType + "'");
        }
    }
}
