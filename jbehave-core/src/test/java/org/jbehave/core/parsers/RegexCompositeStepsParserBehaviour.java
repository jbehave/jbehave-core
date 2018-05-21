package org.jbehave.core.parsers;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.CompositeStep;
import org.jbehave.core.steps.StepType;
import org.junit.Test;

/**
 * @author Valery Yatsynovich
 */
public class RegexCompositeStepsParserBehaviour {

    private static final String NL = "\n";

    private CompositeStepsParser parser = new RegexCompositeStepsParser();

    @Test
    public void shouldParseEmptySteps() {
        List<CompositeStep> compositeSteps = parser.parseCompositeSteps(EMPTY);
        assertThat(compositeSteps, equalTo(Collections.<CompositeStep>emptyList()));
    }

    @Test
    public void shouldParseSingleCompositeStep() {
        String compositeStepsAsText = "Composite: Given a composite step" + NL+
                "Given a step" + NL +
                "Then another step";
        List<CompositeStep> compositeSteps = parser.parseCompositeSteps(compositeStepsAsText);
        assertThat(compositeSteps.size(), equalTo(1));
        assertCompositeStep(compositeSteps.get(0), StepType.GIVEN, "a composite step",
                Arrays.asList("Given a step", "Then another step"));
    }

    @Test
    public void shouldParseTwoCompositeStep() {
        String compositeStepsAsText = "Composite: Given the first composite step" + NL+
                "Given a step" + NL +
                "Composite: When the second composite step" + NL+
                "Then another step" + NL;
        List<CompositeStep> compositeSteps = parser.parseCompositeSteps(compositeStepsAsText);
        assertThat(compositeSteps.size(), equalTo(2));
        assertCompositeStep(compositeSteps.get(0), StepType.GIVEN, "the first composite step",
                Collections.singletonList("Given a step"));
        assertCompositeStep(compositeSteps.get(1), StepType.WHEN, "the second composite step",
                Collections.singletonList("Then another step"));
    }

    @Test
    public void shouldParseCompositeStepWithCustomLocale() {
        String compositeStepsAsText = "Композитный: Дано композитный шаг" + NL+
                "Дано шаг";
        CompositeStepsParser localizedParser = new RegexCompositeStepsParser(new LocalizedKeywords(new Locale("ru")));
        List<CompositeStep> compositeSteps = localizedParser.parseCompositeSteps(compositeStepsAsText);
        assertThat(compositeSteps.size(), equalTo(1));
        assertCompositeStep(compositeSteps.get(0), StepType.GIVEN, "композитный шаг",
                Collections.singletonList("Дано шаг"));
    }

    private void assertCompositeStep(CompositeStep compositeStep, StepType stepType, String stepWithoutStartingWord,
            List<String> steps) {
        assertThat(compositeStep.getStepType(), equalTo(stepType));
        assertThat(compositeStep.getStepWithoutStartingWord(), equalTo(stepWithoutStartingWord));
        assertThat(compositeStep.getSteps(), equalTo(steps));
    }
}
