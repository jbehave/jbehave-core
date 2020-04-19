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
        Matcher findingCompositeMatcher = findingCompositePattern().matcher(compositeAsText);
        String compositePattern = NONE;
        int priority = 0;
        if (findingCompositeMatcher.find()) {
            compositePattern = findingCompositeMatcher.group(1).trim();
            String priorityGroup = findingCompositeMatcher.group(2);
            if (priorityGroup != null) {
                priority = Integer.parseInt(priorityGroup);
            }
        }
        String compositeWithoutKeyword = removeStart(compositeAsText, keywords().composite()).trim();
        String compositeWithoutName = removeStart(compositeWithoutKeyword, compositePattern);
        compositeWithoutName = startingWithNL(compositeWithoutName);
        List<String> steps = findSteps(compositeWithoutName);
        StepType stepType = keywords().stepTypeFor(compositePattern);
        String stepWithoutStartingWord = keywords().stepWithoutStartingWord(compositePattern, stepType);
        return new Composite(stepType, stepWithoutStartingWord, priority, steps);
    }

    // Regex Patterns
    private Pattern findingCompositePattern() {
        String startingWords = concatenateInitialStartingWords();
        return compile(keywords().composite() + "(.*?)\\s*(?:\n\\s*" + keywords().priority() + "\\s*(\\d+)\\s*)?"
                + "(" + startingWords + ".*|\\s*$)", DOTALL);
    }
}
