package org.jbehave.core.parsers;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.Steps;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableTransformers;

/**
 * Pattern-based story parser, which uses the keywords provided to parse the
 * textual story into a {@link Story}.
 */
public class RegexStoryParser implements StoryParser {

    private static final String NONE = "";
    private final Keywords keywords;
    private final ExamplesTableFactory tableFactory;

    public RegexStoryParser() {
        this(new LoadFromClasspath(), new TableTransformers());
    }

    public RegexStoryParser(ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(new LocalizedKeywords(), resourceLoader, tableTransformers);
    }

    public RegexStoryParser(Keywords keywords, ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(keywords, new ExamplesTableFactory(keywords, resourceLoader, tableTransformers));
    }

    public RegexStoryParser(ExamplesTableFactory tableFactory) {
        this(tableFactory.keywords(), tableFactory);
    }

    public RegexStoryParser(Keywords keywords, ExamplesTableFactory tableFactory) {
        this.keywords = keywords;
        this.tableFactory = tableFactory;
        // must ensure that both are using same keywords
        this.tableFactory.useKeywords(keywords);
    }

    public RegexStoryParser(Configuration configuration) {
        this.keywords = configuration.keywords();
        this.tableFactory = new ExamplesTableFactory(configuration);
    }
    
    public Story parseStory(String storyAsText) {
        return parseStory(storyAsText, null);
    }

    public Story parseStory(String storyAsText, String storyPath) {
        Description description = parseDescriptionFrom(storyAsText);
        Meta meta = parseStoryMetaFrom(storyAsText);
        Narrative narrative = parseNarrativeFrom(storyAsText);
        GivenStories givenStories = parseGivenStories(storyAsText);
        Lifecycle lifecycle = parseLifecycle(storyAsText);
        List<Scenario> scenarios = parseScenariosFrom(storyAsText);
        Story story = new Story(storyPath, description, meta, narrative, givenStories, lifecycle, scenarios);
        if (storyPath != null) {
            story.namedAs(new File(storyPath).getName());
        }
        return story;
    }

    private Description parseDescriptionFrom(String storyAsText) {
        Matcher findingDescription = findingDescription().matcher(storyAsText);
        if (findingDescription.matches()) {
            return new Description(findingDescription.group(1).trim());
        }
        return Description.EMPTY;
    }

    private Meta parseStoryMetaFrom(String storyAsText) {
        Matcher findingMeta = findingStoryMeta().matcher(preScenarioText(storyAsText));
        if (findingMeta.matches()) {
            String meta = findingMeta.group(1).trim();
            return Meta.createMeta(meta, keywords);
        }
        return Meta.EMPTY;
    }

    private String preScenarioText(String storyAsText) {
        String[] split = storyAsText.split(keywords.scenario());
        return split.length > 0 ? split[0] : storyAsText;
    }

    private Narrative parseNarrativeFrom(String storyAsText) {
        Matcher findingNarrative = findingNarrative().matcher(storyAsText);
        if (findingNarrative.matches()) {
            String narrative = findingNarrative.group(1).trim();
            return createNarrative(narrative);
        }
        return Narrative.EMPTY;
    }

    private Narrative createNarrative(String narrative) {
        Matcher findingElements = findingNarrativeElements().matcher(narrative);
        if (findingElements.matches()) {
            String inOrderTo = findingElements.group(1).trim();
            String asA = findingElements.group(2).trim();
            String iWantTo = findingElements.group(3).trim();
            return new Narrative(inOrderTo, asA, iWantTo);
        }
        Matcher findingAlternativeElements = findingAlternativeNarrativeElements().matcher(narrative);
        if (findingAlternativeElements.matches()) {            
            String asA = findingAlternativeElements.group(1).trim();
            String iWantTo = findingAlternativeElements.group(2).trim();
            String soThat = findingAlternativeElements.group(3).trim();
            return new Narrative("", asA, iWantTo, soThat);
        }
        return Narrative.EMPTY;
    }
    
    private GivenStories parseGivenStories(String storyAsText) {
        String scenarioKeyword = keywords.scenario();
        // use text before scenario keyword, if found
        String beforeScenario = "";
        if (StringUtils.contains(storyAsText, scenarioKeyword)) {
            beforeScenario = StringUtils.substringBefore(storyAsText, scenarioKeyword);
        }
        Matcher findingGivenStories = findingStoryGivenStories().matcher(beforeScenario);
        String givenStories = findingGivenStories.find() ? findingGivenStories.group(1).trim() : NONE;
        return new GivenStories(givenStories);
    }

    private Lifecycle parseLifecycle(String storyAsText) {
        String scenarioKeyword = keywords.scenario();
        // use text before scenario keyword, if found
        String beforeScenario = "";
        if (StringUtils.contains(storyAsText, scenarioKeyword)) {
            beforeScenario = StringUtils.substringBefore(storyAsText, scenarioKeyword);
        }
        Matcher findingLifecycle = findingLifecycle().matcher(beforeScenario);
        String lifecycle = findingLifecycle.find() ? findingLifecycle.group(1).trim() : NONE;
        Matcher findingBeforeAndAfter = compile(".*" + keywords.before() + "(.*)\\s*" + keywords.after() + "(.*)\\s*", DOTALL).matcher(lifecycle);
        if ( findingBeforeAndAfter.matches() ){
            String beforeLifecycle = findingBeforeAndAfter.group(1).trim();
            Steps beforeSteps = parseBeforeLifecycle(beforeLifecycle);
            String afterLifecycle = findingBeforeAndAfter.group(2).trim();
            Steps[] afterSteps = parseAfterLifecycle(afterLifecycle);
            return new Lifecycle(beforeSteps, afterSteps);
        }
        Matcher findingBefore = compile(".*" + keywords.before() + "(.*)\\s*", DOTALL).matcher(lifecycle);
        if ( findingBefore.matches() ){
            String beforeLifecycle = findingBefore.group(1).trim();
            Steps beforeSteps = parseBeforeLifecycle(beforeLifecycle);
            return new Lifecycle(beforeSteps, new Steps(new ArrayList<String>()));
        }
        Matcher findingAfter = compile(".*" + keywords.after() + "(.*)\\s*", DOTALL).matcher(lifecycle);
        if ( findingAfter.matches() ){
            Steps beforeSteps = Steps.EMPTY;
            String afterLifecycle = findingAfter.group(1).trim();
            Steps[] afterSteps = parseAfterLifecycle(afterLifecycle);
            return new Lifecycle(beforeSteps, afterSteps);
        }
        return Lifecycle.EMPTY;
    }

    private Steps parseBeforeLifecycle(String lifecycleAsText) {
        return new Steps(findSteps(startingWithNL(lifecycleAsText)));
    }

    private Steps[] parseAfterLifecycle(String lifecycleAsText) {
        List<Steps> list = new ArrayList<Steps>();
        for (String byOutcome : lifecycleAsText.split(keywords.outcome()) ){ 
            byOutcome = byOutcome.trim();
            if ( byOutcome.isEmpty() ) continue;
            String outcomeAsText = findOutcome(byOutcome);
            String filtersAsText = findFilters(removeStart(byOutcome, outcomeAsText));
            List<String> steps = findSteps(startingWithNL(removeStart(byOutcome, filtersAsText)));
            list.add(new Steps(parseOutcome(outcomeAsText), parseFilters(filtersAsText), steps));
        }
        return list.toArray(new Steps[list.size()]);
    }

    private String findOutcome(String stepsByOutcome) {
        Matcher findingOutcome = findingLifecycleOutcome().matcher(stepsByOutcome);
        if ( findingOutcome.matches() ){
            return findingOutcome.group(1).trim();
        }
        return keywords.outcomeAny();
    }

    private Outcome parseOutcome(String outcomeAsText) {
        if ( outcomeAsText.equals(keywords.outcomeSuccess()) ){
            return Outcome.SUCCESS;
        } else if ( outcomeAsText.equals(keywords.outcomeFailure()) ){
            return Outcome.FAILURE;
        }
        return Outcome.ANY;
    }

    private String findFilters(String stepsByFilters) {
        Matcher findingFilters = findingLifecycleFilters().matcher(stepsByFilters.trim());
        if ( findingFilters.matches() ){
            return findingFilters.group(1).trim();
        }
        return NONE;
    }

    private String parseFilters(String filtersAsText) {
        return removeStart(filtersAsText, keywords.metaFilter()).trim();
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

        // use text after scenario keyword, if found
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
        String scenarioWithoutKeyword = removeStart(scenarioAsText, keywords.scenario()).trim();
        String scenarioWithoutTitle = removeStart(scenarioWithoutKeyword, title);
        scenarioWithoutTitle = startingWithNL(scenarioWithoutTitle);
        Meta meta = findScenarioMeta(scenarioWithoutTitle);
        ExamplesTable examplesTable = findExamplesTable(scenarioWithoutTitle);
        GivenStories givenStories = findScenarioGivenStories(scenarioWithoutTitle);
        if (givenStories.requireParameters()) {
            givenStories.useExamplesTable(examplesTable);
        }
        List<String> steps = findSteps(scenarioWithoutTitle);
        return new Scenario(title, meta, givenStories, examplesTable, steps);
    }

    private String startingWithNL(String text) {
        if ( !text.startsWith("\n") ){ // always ensure starts with newline
            return "\n" + text;
        }
        return text;
    }

    private String findScenarioTitle(String scenarioAsText) {
        Matcher findingTitle = findingScenarioTitle().matcher(scenarioAsText);
        return findingTitle.find() ? findingTitle.group(1).trim() : NONE;
    }

    private Meta findScenarioMeta(String scenarioAsText) {
        Matcher findingMeta = findingScenarioMeta().matcher(scenarioAsText);
        if (findingMeta.matches()) {
            String meta = findingMeta.group(1).trim();
            return Meta.createMeta(meta, keywords);
        }
        return Meta.EMPTY;
    }

    private ExamplesTable findExamplesTable(String scenarioAsText) {
        Matcher findingTable = findingExamplesTable().matcher(scenarioAsText);
        String tableInput = findingTable.find() ? findingTable.group(1).trim() : NONE;
        return tableFactory.createExamplesTable(tableInput);
    }

    private GivenStories findScenarioGivenStories(String scenarioAsText) {
        Matcher findingGivenStories = findingScenarioGivenStories().matcher(scenarioAsText);
        String givenStories = findingGivenStories.find() ? findingGivenStories.group(1).trim() : NONE;
        return new GivenStories(givenStories);
    }

    private List<String> findSteps(String stepsAsText) {
        Matcher matcher = findingSteps().matcher(stepsAsText);
        List<String> steps = new ArrayList<String>();
        int startAt = 0;
        while (matcher.find(startAt)) {
            steps.add(StringUtils.substringAfter(matcher.group(1), "\n"));
            startAt = matcher.start(4);
        }
        return steps;
    }

    // Regex Patterns

    private Pattern findingDescription() {
        String metaOrNarrativeOrLifecycleOrScenario = concatenateWithOr(keywords.meta(), keywords.narrative(), keywords.lifecycle(), keywords.scenario());
        return compile("(.*?)(" + metaOrNarrativeOrLifecycleOrScenario + ").*", DOTALL);
    }

    private Pattern findingStoryMeta() {
        String narrativeOrLifecycleOrGivenStories = concatenateWithOr(keywords.narrative(), keywords.lifecycle(), keywords.givenStories());
        return compile(".*" + keywords.meta() + "(.*?)\\s*(\\Z|" + narrativeOrLifecycleOrGivenStories + ").*", DOTALL);
    }

    private Pattern findingNarrative() {
        String givenStoriesOrLifecycleOrScenario = concatenateWithOr(keywords.givenStories(), keywords.lifecycle(), keywords.scenario());
        return compile(".*" + keywords.narrative() + "(.*?)\\s*(" + givenStoriesOrLifecycleOrScenario + ").*", DOTALL);
    }

    private Pattern findingNarrativeElements() {
        return compile(".*" + keywords.inOrderTo() + "(.*)\\s*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo()
                + "(.*)", DOTALL);
    }

    private Pattern findingAlternativeNarrativeElements() {
        return compile(".*" + keywords.asA() + "(.*)\\s*" + keywords.iWantTo() + "(.*)\\s*" + keywords.soThat()
                + "(.*)", DOTALL);
    }
    
    private Pattern findingStoryGivenStories() {
        String lifecycleOrScenario = concatenateWithOr(keywords.lifecycle(), keywords.scenario());
        return compile(".*" + keywords.givenStories() + "(.*?)\\s*(\\Z|" + lifecycleOrScenario + ").*", DOTALL);
    }
    
    private Pattern findingLifecycle() {
        return compile(".*" + keywords.lifecycle() + "\\s*(.*)", DOTALL);
    }
    
    private Pattern findingLifecycleOutcome() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        String outcomes = concatenateWithOr(keywords.outcomeAny(), keywords.outcomeSuccess(), keywords.outcomeFailure());
        return compile("\\s*("+ outcomes +")\\s*(" + keywords.metaFilter() + "|" + startingWords + ").*", DOTALL);
    }

    private Pattern findingLifecycleFilters() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        String filters = concatenateWithOr(keywords.metaFilter());
        return compile("\\s*("+ filters +"[\\w\\+\\-\\_\\s]*)(" + startingWords + ").*", DOTALL);
    }

    private Pattern findingScenarioTitle() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile(keywords.scenario() + "((.)*?)\\s*(" + keywords.meta() + "|" + startingWords + ").*", DOTALL);
    }

    private Pattern findingScenarioMeta() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile(".*" + keywords.meta() + "(.*?)\\s*(" + keywords.givenStories() + "|" + startingWords + ").*",
                DOTALL);
    }

    private Pattern findingScenarioGivenStories() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile("\\n" + keywords.givenStories() + "((.|\\n)*?)\\s*(" + startingWords + ").*", DOTALL);
    }

    private Pattern findingSteps() {
        String initialStartingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        String followingStartingWords = concatenateWithOr("\\n", "\\s", keywords.startingWords());
        return compile(
                "((" + initialStartingWords + ")\\s(.)*?)\\s*(\\Z|" + followingStartingWords + "|\\n"
                        + keywords.examplesTable() + ")", DOTALL);
    }

    private Pattern findingExamplesTable() {
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
        return StringUtils.removeEnd(builder.toString(), "|"); // remove last "|"
    }

}
