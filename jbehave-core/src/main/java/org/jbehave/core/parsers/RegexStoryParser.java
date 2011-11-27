package org.jbehave.core.parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.removeStart;

/**
 * Pattern-based story parser, which uses the keywords provided to parse the
 * textual story into a {@link Story}.
 */
public class RegexStoryParser implements StoryParser {

    private static final String NONE = "";
    private final Keywords keywords;
    private final ExamplesTableFactory tableFactory;

    public RegexStoryParser() {
        this(new LocalizedKeywords());
    }

    public RegexStoryParser(Keywords keywords) {
        this(keywords, new ExamplesTableFactory());
    }

    public RegexStoryParser(ExamplesTableFactory tableFactory) {
        this(new LocalizedKeywords(), tableFactory);
    }

    public RegexStoryParser(Keywords keywords, ExamplesTableFactory tableFactory) {
        this.keywords = keywords;
        this.tableFactory = tableFactory;
    }

    public Story parseStory(String storyAsText) {
        return parseStory(storyAsText, null);
    }

    public Story parseStory(String storyAsText, String storyPath) {
        Description description = parseDescriptionFrom(storyAsText);
        Meta meta = parseStoryMetaFrom(storyAsText);
        Narrative narrative = parseNarrativeFrom(storyAsText);
        List<Scenario> scenarios = parseScenariosFrom(storyAsText);
        Story story = new Story(storyPath, description, meta, narrative, scenarios);
        if (storyPath != null) {
            story.namedAs(new File(storyPath).getName());
        }
        return story;
    }

    private Description parseDescriptionFrom(String storyAsText) {
        Matcher findingDescription = patternToPullDescriptionIntoGroupOne().matcher(storyAsText);
        if (findingDescription.matches()) {
            return new Description(findingDescription.group(1).trim());
        }
        return Description.EMPTY;
    }

    private Meta parseStoryMetaFrom(String storyAsText) {
        Matcher findingMeta = patternToPullStoryMetaIntoGroupOne().matcher(preScenarioText(storyAsText));
        if (findingMeta.matches()) {
            String meta = findingMeta.group(1).trim();
            return createMeta(meta);
        }
        return Meta.EMPTY;
    }

    private String preScenarioText(String storyAsText) {
        String[] split = storyAsText.split(keywords.scenario());
        return split.length > 0 ? split[0] : storyAsText;
    }

    private Meta createMeta(String meta) {
        List<String> properties = new ArrayList<String>();
        for (String property : meta.split(keywords.metaProperty())) {
            if (StringUtils.isNotBlank(property)) {
                String beforeIgnorable = StringUtils.substringBefore(property,keywords.ignorable());
                if ( StringUtils.isNotBlank(beforeIgnorable)){
                    properties.add(beforeIgnorable);
                }
            }
        }
        return new Meta(properties);
    }

    private Narrative parseNarrativeFrom(String storyAsText) {
        Matcher findingNarrative = patternToPullNarrativeIntoGroupOne().matcher(storyAsText);
        if (findingNarrative.matches()) {
            String narrative = findingNarrative.group(1).trim();
            return createNarrative(narrative);
        }
        return Narrative.EMPTY;
    }

    private Narrative createNarrative(String narrative) {
        Pattern findElements = patternToPullNarrativeElementsIntoGroups();
        Matcher findingElements = findElements.matcher(narrative);
        if (findingElements.matches()) {
            String inOrderTo = findingElements.group(1).trim();
            String asA = findingElements.group(2).trim();
            String iWantTo = findingElements.group(3).trim();
            return new Narrative(inOrderTo, asA, iWantTo);
        }
        return Narrative.EMPTY;
    }

    private List<Scenario> parseScenariosFrom(String storyAsText) {
        List<Scenario> parsed = new ArrayList<Scenario>();
        for (String scenarioAsText : splitScenarios(storyAsText)) {
            parsed.add(parseScenario(scenarioAsText));
        }
        return parsed;
    }

    private List<String> splitScenarios(String storyAsText) {
        List<String> scenarios = new ArrayList<String>();
        String scenarioKeyword = keywords.scenario();

        // remove anything after scenario keyword, if found
        if (StringUtils.contains(storyAsText, scenarioKeyword)) {
            storyAsText = StringUtils.substringAfter(storyAsText, scenarioKeyword);
        }

        for (String scenarioAsText : storyAsText.split(scenarioKeyword)) {
            if (scenarioAsText.trim().length() > 0) {
                scenarios.add(scenarioKeyword + "\n" + scenarioAsText);
            }
        }
        return scenarios;
    }

    private Scenario parseScenario(String scenarioAsText) {
        String title = findScenarioTitle(scenarioAsText);
        String scenarioWithoutTitle = removeStart(scenarioAsText, title);
        Meta meta = findScenarioMeta(scenarioWithoutTitle);
        ExamplesTable examplesTable = findExamplesTable(scenarioWithoutTitle);
        GivenStories givenStories = findGivenStories(scenarioWithoutTitle);
        if (givenStories.requireParameters()) {
            givenStories.useExamplesTable(examplesTable);
        }
        List<String> steps = findSteps(scenarioWithoutTitle);
        return new Scenario(title, meta, givenStories, examplesTable, steps);
    }

    private String findScenarioTitle(String scenarioAsText) {
        Matcher findingTitle = patternToPullScenarioTitleIntoGroupOne().matcher(scenarioAsText);
        return findingTitle.find() ? findingTitle.group(1).trim() : NONE;
    }

    private Meta findScenarioMeta(String scenarioAsText) {
        Matcher findingMeta = patternToPullScenarioMetaIntoGroupOne().matcher(scenarioAsText);
        if (findingMeta.matches()) {
            String meta = findingMeta.group(1).trim();
            return createMeta(meta);
        }
        return Meta.EMPTY;
    }

    private ExamplesTable findExamplesTable(String scenarioAsText) {
        Matcher findingTable = patternToPullExamplesTableIntoGroupOne().matcher(scenarioAsText);
        String tableInput = findingTable.find() ? findingTable.group(1).trim() : NONE;
        return tableFactory.createExamplesTable(tableInput);
    }

    private GivenStories findGivenStories(String scenarioAsText) {
        Matcher findingGivenStories = patternToPullGivenStoriesIntoGroupOne().matcher(scenarioAsText);
        String givenStories = findingGivenStories.find() ? findingGivenStories.group(1).trim() : NONE;
        return new GivenStories(givenStories);
    }

    private List<String> findSteps(String scenarioAsText) {
        Matcher matcher = patternToPullStepsIntoGroupOne().matcher(scenarioAsText);
        List<String> steps = new ArrayList<String>();
        int startAt = 0;
        while (matcher.find(startAt)) {
            steps.add(StringUtils.substringAfter(matcher.group(1), "\n"));
            startAt = matcher.start(4);
        }
        return steps;
    }

    // Regex Patterns

    private Pattern patternToPullDescriptionIntoGroupOne() {
        String metaOrNarrativeOrScenario = concatenateWithOr(keywords.meta(), keywords.narrative(), keywords.scenario());
        return compile("(.*?)(" + metaOrNarrativeOrScenario + ").*", DOTALL);
    }

    private Pattern patternToPullStoryMetaIntoGroupOne() {
        return compile(".*" + keywords.meta() + "(.*?)\\s*(\\Z|" + keywords.narrative() + ").*", DOTALL);
    }

    private Pattern patternToPullNarrativeIntoGroupOne() {
        return compile(".*" + keywords.narrative() + "(.*?)\\s*(" + keywords.scenario() + ").*", DOTALL);
    }

    private Pattern patternToPullNarrativeElementsIntoGroups() {
        return compile(".*" + keywords.inOrderTo() + "(.*)\\s*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo()
                + "(.*)", DOTALL);
    }

    private Pattern patternToPullScenarioTitleIntoGroupOne() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile(keywords.scenario() + "((.)*?)\\s*(" + keywords.meta() + "|" + startingWords + ").*", DOTALL);
    }

    private Pattern patternToPullScenarioMetaIntoGroupOne() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile(".*" + keywords.meta() + "(.*?)\\s*(" + keywords.givenStories() + "|" + startingWords + ").*",
                DOTALL);
    }

    private Pattern patternToPullGivenStoriesIntoGroupOne() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile("\\n" + keywords.givenStories() + "((.|\\n)*?)\\s*(" + startingWords + ").*", DOTALL);
    }

    private Pattern patternToPullStepsIntoGroupOne() {
        String initialStartingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        String followingStartingWords = concatenateWithOr("\\n", "\\s", keywords.startingWords());
        return compile(
                "((" + initialStartingWords + ") (.)*?)\\s*(\\Z|" + followingStartingWords + "|\\n"
                        + keywords.examplesTable() + ")", DOTALL);
    }

    private Pattern patternToPullExamplesTableIntoGroupOne() {
        return compile("\\n" + keywords.examplesTable() + "\\s*(.*)", DOTALL);
    }

    private String concatenateWithOr(String... keywords) {
        return concatenateWithOr(null, null, keywords);
    }

    private String concatenateWithOr(String beforeKeyword, String afterKeyword, String[] keywords) {
        StringBuilder builder = new StringBuilder();
        String before = beforeKeyword != null ? beforeKeyword : NONE;
        String after = afterKeyword != null ? afterKeyword : NONE;
        for (String keyword : keywords) {
            builder.append(before).append(keyword).append(after).append("|");
        }
        return StringUtils.chomp(builder.toString(), "|"); // chop off the last
                                                           // "|"
    }

}
