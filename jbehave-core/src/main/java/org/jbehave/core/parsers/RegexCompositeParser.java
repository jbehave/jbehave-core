package org.jbehave.core.parsers;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Composite;
import org.jbehave.core.steps.StepType;

/**
 * Pattern-based composite parser, which uses the keywords provided to parse the
 * textual composite steps into a list of {@link Composite}.
 *
 * @author Mauro Talevi
 * @author Valery Yatsynovich
 */
public class RegexCompositeParser extends AbstractRegexParser implements CompositeParser {

    public RegexCompositeParser() {
        super();
    }

    public RegexCompositeParser(Keywords keywords) {
        super(keywords);
    }

    @Override
    public List<Composite> parseComposites(String compositesAsText) {
        List<Composite> parsed = new ArrayList<>();
        for (String compositeAsText : splitElements(compositesAsText, keywords().composite())) {
            parsed.add(parseComposite(compositeAsText));
        }
        return parsed;
    }

    private Composite parseComposite(String compositeAsText) {
        String compositePattern = findCompositePattern(compositeAsText);
        String compositeWithoutKeyword = removeStart(compositeAsText, keywords().composite()).trim();
        String compositeWithoutName = removeStart(compositeWithoutKeyword, compositePattern);
        compositeWithoutName = startingWithNL(compositeWithoutName);
        List<String> steps = findSteps(compositeWithoutName);
        StepType stepType = keywords().stepTypeFor(compositePattern);
        String stepWithoutStartingWord = keywords().stepWithoutStartingWord(compositePattern, stepType);
        return new Composite(stepType, stepWithoutStartingWord, steps);
    }

    private String findCompositePattern(String compositeAsText) {
        Matcher findingPattern = findingCompositePattern().matcher(compositeAsText);
        return findingPattern.find() ? findingPattern.group(1).trim() : NONE;
    }

    // Regex Patterns
    private Pattern findingCompositePattern() {
        String startingWords = concatenateInitialStartingWords();
        return compile(keywords().composite() + "((.)*?)\\s*(" + startingWords + ").*", DOTALL);
    }
}
