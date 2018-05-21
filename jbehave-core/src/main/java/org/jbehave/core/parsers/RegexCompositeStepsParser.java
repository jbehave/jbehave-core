package org.jbehave.core.parsers;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.CompositeStep;
import org.jbehave.core.steps.StepType;

/**
 * Pattern-based composite steps parser, which uses the keywords provided to parse the
 * textual composite steps into a {@link CompositeStep}.
 *
 * @author Valery Yatsynovich
 */
public class RegexCompositeStepsParser extends AbstractRegexParser implements CompositeStepsParser {

    public RegexCompositeStepsParser() {
        super();
    }

    public RegexCompositeStepsParser(Keywords keywords) {
        super(keywords);
    }

    @Override
    public List<CompositeStep> parseCompositeSteps(String compositeStepsAsText) {
        List<CompositeStep> parsed = new ArrayList<>();
        for (String compositeStepAsText : splitElements(compositeStepsAsText, keywords().composite())) {
            parsed.add(parseCompositeStep(compositeStepAsText));
        }
        return parsed;
    }

    private CompositeStep parseCompositeStep(String compositeStepAsText) {
        String name = findCompositeStepName(compositeStepAsText);
        String compositeStepWithoutKeyword = removeStart(compositeStepAsText, keywords().composite()).trim();
        String compositeStepWithoutName = removeStart(compositeStepWithoutKeyword, name);
        compositeStepWithoutName = startingWithNL(compositeStepWithoutName);
        List<String> steps = findSteps(compositeStepWithoutName);
        StepType stepType = keywords().stepTypeFor(name);
        String stepWithoutStartingWord = keywords().stepWithoutStartingWord(name, stepType);
        return new CompositeStep(stepType, stepWithoutStartingWord, steps);
    }

    private String findCompositeStepName(String compositeStepAsText) {
        Matcher findingName = findingCompositeStepName().matcher(compositeStepAsText);
        return findingName.find() ? findingName.group(1).trim() : NONE;
    }

    // Regex Patterns

    private Pattern findingCompositeStepName() {
        String startingWords = concatenateInitialStartingWords();
        return compile(keywords().composite() + "((.)*?)\\s*(" + startingWords + ").*", DOTALL);
    }
}
