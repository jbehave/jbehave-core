package org.jbehave.core.parsers;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.*;

/**
 * Pattern-based story parser, which uses the keywords provided to parse the
 * textual story into a {@link Story}, which comprises of a collection of
 * {@link Scenario}s, each of which contains a number of steps.
 */
public class RegexStoryParser implements StoryParser {

    private static final String NONE = "";
    private static final String COMMA = ",";
    private final Keywords keywords;
    private String storyPath;

    public RegexStoryParser() {
        this(new LocalizedKeywords());
    }

    public RegexStoryParser(Keywords keywords) {
        this.keywords = keywords;
    }

    public Story parseStory(String storyAsText) {
        return parseStory(storyAsText, null);
    }

    public Story parseStory(String storyAsText, String storyPath) {
        this.storyPath = storyPath;
        Description description = parseDescriptionFrom(storyAsText);
        Narrative narrative = parseNarrativeFrom(storyAsText);
        List<Scenario> scenarios = parseScenariosFrom(storyAsText);
        return new Story(description, narrative, storyPath, scenarios);
    }

    private Description parseDescriptionFrom(String storyAsText) {
        String concatenatedKeywords = concatenateWithOr(keywords.narrative(), keywords.scenario());
        Pattern findDescription = compile("(.*?)(" + concatenatedKeywords + ").*", DOTALL);
        Matcher findingDescription = findDescription.matcher(storyAsText);
        if (findingDescription.matches()) {
            return new Description(findingDescription.group(1).trim());
        }
        return Description.EMPTY;
    }

    private Narrative parseNarrativeFrom(String storyAsText) {
        Pattern findNarrative = compile(".*" + keywords.narrative() + "(.*?)\\s*(" + keywords.scenario() + ").*", DOTALL);
        Matcher findingNarrative = findNarrative.matcher(storyAsText);
        if (findingNarrative.matches()) {
            String narrative = findingNarrative.group(1).trim();
            return createNarrative(narrative);
        }
        return Narrative.EMPTY;
    }

    private Narrative createNarrative(String narrative) {
        Pattern findElements = compile(".*" + keywords.inOrderTo() + "(.*)\\s*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo() + "(.*)", DOTALL);
        Matcher findingElements = findElements.matcher(narrative);
        if (findingElements.matches()) {
            String inOrderTo = findingElements.group(1).trim();
            String asA = findingElements.group(2).trim();
            String iWantTo = findingElements.group(3).trim();
            return new Narrative(inOrderTo, asA, iWantTo);
        }
        return Narrative.EMPTY;
    }

    private List<Scenario> parseScenariosFrom(
            String storyAsText) {
        List<Scenario> parsed = new ArrayList<Scenario>();
        List<String> scenariosAsText = splitScenarios(storyAsText);
        for (String scenarioAsText : scenariosAsText) {
            parsed.add(parseScenario(scenarioAsText));
        }
        return parsed;
    }

    private Scenario parseScenario(String scenarioAsText) {
        String title = findTitle(scenarioAsText);
        ExamplesTable table = findTable(scenarioAsText);
        List<String> givenStoryPaths = findGivenStoryPaths(scenarioAsText);
        List<String> steps = findSteps(scenarioAsText);
        return new Scenario(title, givenStoryPaths, table, steps);
    }

    private String findTitle(String scenarioAsText) {
        Matcher findingTitle = patternToPullScenarioTitleIntoGroupOne()
                .matcher(scenarioAsText);
        return findingTitle.find() ? findingTitle.group(1).trim() : NONE;
    }

    private ExamplesTable findTable(String scenarioAsText) {
        Matcher findingTable = patternToPullExamplesTableIntoGroupOne()
                .matcher(scenarioAsText);
        String table = findingTable.find() ? findingTable.group(1).trim() : NONE;
        return new ExamplesTable(table, keywords.examplesTableHeaderSeparator(), keywords.examplesTableValueSeparator());
    }

    private List<String> findGivenStoryPaths(String scenarioAsText) {
        Matcher findingGivenStories = patternToPullGivenStoriesIntoGroupOne()
                .matcher(scenarioAsText);
        String givenStories = findingGivenStories.find() ? findingGivenStories.group(1).trim() : NONE;
        List<String> givenStoryPaths = new ArrayList<String>();
        for (String storyPath : givenStories.split(COMMA)) {
            String trimmed = storyPath.trim();
            if (trimmed.length() > 0) {
                givenStoryPaths.add(trimmed);
            }
        }
        return givenStoryPaths;
    }

    private List<String> findSteps(String scenarioAsText) {
        Matcher matcher = patternToPullOutSteps().matcher(scenarioAsText);
        List<String> steps = new ArrayList<String>();
        int startAt = 0;
        while (matcher.find(startAt)) {
            steps.add(matcher.group(1));
            startAt = matcher.start(4);
        }
        return steps;
    }

    @SuppressWarnings("serial")
    public static class InvalidPatternException extends RuntimeException {
        public InvalidPatternException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    protected List<String> splitScenarios(String storyAsText) {
        return splitScenariosWithKeyword(storyAsText);
    }

    protected List<String> splitScenariosWithKeyword(String storyAsText) {
        List<String> scenarios = new ArrayList<String>();
        String scenarioKeyword = keywords.scenario();

        String allScenarios = null;
        // chomp off anything before first keyword, if found
        int keywordIndex = storyAsText.indexOf(scenarioKeyword);
        if (keywordIndex != -1) {
            allScenarios = storyAsText.substring(keywordIndex);
        } else { // use all stories in file
            allScenarios = storyAsText;
        }

        for (String scenario : allScenarios.split(scenarioKeyword)) {
            if (scenario.trim().length() > 0) {
                scenarios.add(scenarioKeyword + scenario);
            }
        }
        return scenarios;
    }

    // This pattern approach causes stack overflow error on Windows
    // http://jbehave.org/documentation/known-issues/regex-stack-overflow-errors

    protected List<String> splitScenariosWithPattern(String storyAsText) {
        Pattern scenarioSplitter = patternToPullScenariosIntoGroupFour();
        Matcher matcher = scenarioSplitter.matcher(storyAsText);
        int startAt = 0;
        List<String> scenarios = new ArrayList<String>();
        try {
            if (matcher.matches()) {
                while (matcher.find(startAt)) {
                    scenarios.add(matcher.group(1));
                    startAt = matcher.start(4);
                }
            } else {
                scenarios.add(storyAsText);
            }
        } catch (StackOverflowError e) {
            String message = "Failed to parse story (see http://jbehave.org/documentation/known-issues/regex-stack-overflow-errors): "
                    + (storyPath != null ? storyPath : storyAsText);
            throw new InvalidPatternException(message, e);
        }
        return scenarios;
    }

    private Pattern patternToPullScenariosIntoGroupFour() {
        String scenario = keywords.scenario();
        return compile(".*?((" + scenario + ") (.|\\s)*?)\\s*(\\Z|" + scenario
                + ").*", DOTALL);
    }

    private Pattern patternToPullGivenStoriesIntoGroupOne() {
        String givenScenarios = keywords.givenStories();
        String concatenatedKeywords = concatenateWithOr(keywords.given(),
                keywords.when(), keywords.then(), keywords.others());
        return compile(".*" + givenScenarios + "((.|\\n)*?)\\s*(" + concatenatedKeywords + ").*");
    }

    private Pattern patternToPullExamplesTableIntoGroupOne() {
        String table = keywords.examplesTable();
        return compile(".*" + table + "\\s*(.*)", DOTALL);
    }

    private Pattern patternToPullScenarioTitleIntoGroupOne() {
        String scenario = keywords.scenario();
        String concatenatedKeywords = concatenateWithOr(keywords.given(),
                keywords.when(), keywords.then(), keywords.others());
        return compile(scenario + "((.|\\n)*?)\\s*(" + concatenatedKeywords + ").*");
    }

    private String concatenateWithOr(String given, String when, String then,
                                     String[] others) {
        return concatenateWithOr(false, given, when, then, others);
    }

    private String concatenateWithSpaceOr(String given, String when,
                                          String then, String[] others) {
        return concatenateWithOr(true, given, when, then, others);
    }

    private String concatenateWithOr(boolean usingSpace, String given,
                                     String when, String then, String[] others) {
        StringBuilder builder = new StringBuilder();
        builder.append(given).append(usingSpace ? "\\s|" : "|");
        builder.append(when).append(usingSpace ? "\\s|" : "|");
        builder.append(then).append(usingSpace ? "\\s|" : "|");
        builder.append(usingSpace ? concatenateWithSpaceOr(others)
                : concatenateWithOr(others));
        return builder.toString();
    }

    private String concatenateWithOr(String... keywords) {
        return concatenateWithOr(false, new StringBuilder(), keywords);
    }

    private String concatenateWithSpaceOr(String... keywords) {
        return concatenateWithOr(true, new StringBuilder(), keywords);
    }

    private String concatenateWithOr(boolean usingSpace, StringBuilder builder,
                                     String[] keywords) {
        for (String other : keywords) {
            builder.append(other).append(usingSpace ? "\\s|" : "|");
        }
        String result = builder.toString();
        return result.substring(0, result.length() - 1); // chop off the last |
    }

    private Pattern patternToPullOutSteps() {
        String givenWhenThen = concatenateWithOr(keywords.given(), keywords
                .when(), keywords.then(), keywords.others());
        String givenWhenThenSpaced = concatenateWithSpaceOr(keywords.given(),
                keywords.when(), keywords.then(), keywords.others());
        String scenario = keywords.scenario();
        String table = keywords.examplesTable();
		return compile("((" + givenWhenThen + ") (.)*?)\\s*(\\Z|"
				+ givenWhenThenSpaced + "|" + scenario + "|"+ table + ")", DOTALL);
    }
}
