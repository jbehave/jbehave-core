package org.jbehave.core.parsers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.regex.Matcher;

import org.jbehave.core.steps.StepType;
import org.junit.jupiter.api.Test;

class RegexPrefixCapturingPatternParserBehaviour {

    private final StepPatternParser parser = new RegexPrefixCapturingPatternParser();

    @Test
    void shouldMatchStepWithPatterns() {
        assertThatPatternMatchesStep(parser, "a house with $numberOfDoors doors and $some windows",
                "a house with 3 doors and 4 windows", true, "numberOfDoors", "some");
        assertThatPatternMatchesStep(parser, "the house on $street", "the house on Easy Street", true, "street");
        assertThatPatternMatchesStep(parser, "$number houses", "5 houses", true, "number");
        assertThatPatternMatchesStep(parser, "my house", "my house", true);
        assertThatPatternMatchesStep(parser, "I toggle the cell at ( $column , $row )",
                "I toggle the cell at ( 3 , 4 )", true, "column", "row");
        assertThatPatternMatchesStep(parser, "$name should ask, \"Why?\"", "Fred should ask, \"Why?\"", true, "name");
        assertThatPatternMatchesStep(parser, "$thousands x 10^3", "2 x 10^3", true, "thousands");
    }

    @Test
    void shouldMatchStepWithPatternsUsingUnderscoresInParameterNames() {
        assertThatPatternMatchesStep(parser, "a house with $number_of_1st_floor_doors doors and $facing_to windows",
                "a house with 3 doors and 4 windows", true, "number_of_1st_floor_doors", "facing_to");
    }

    @Test
    void shouldMatchStepWithPatternsUsingNumbersInParameterNames() {
        assertThatPatternMatchesStep(parser, "a house with $numberOf1stFloorDoors doors and $facing2 windows",
                "a house with 3 doors and 4 windows", true, "numberOf1stFloorDoors", "facing2");
    }

    @Test
    void shouldMatchStepWithPatternsUsingAccentsInParameterNames() {
        assertThatPatternMatchesStep(parser, "une maison avec $numérosDesPortes portes et $quelques fenêtres",
                "une maison avec 3 portes et 4 fenêtres", true, "numérosDesPortes", "quelques");
        assertThatPatternMatchesStep(parser, "ein Haus mit $anzahlDerTüren Türen und $einige Fenster",
                "ein Haus mit 3 Türen und 4 Fenster", true, "anzahlDerTüren", "einige");        
    }
    
    @Test
    void shouldMatchStepWithPatternsUsingCustomPrefix() {
        RegexPrefixCapturingPatternParser parser = new RegexPrefixCapturingPatternParser("%");
        assertThat(parser.getPrefix(), equalTo("%"));
        assertThat(parser.toString(), containsString("prefix=%"));
        assertThatPatternMatchesStep(parser, "a house with %numberOfDoors doors and %some windows",
                "a house with 3 doors and 4 windows", true, "numberOfDoors", "some");
        assertThatPatternMatchesStep(parser, "the house on %street", "the house on Easy Street", true, "street");
        assertThatPatternMatchesStep(parser, "%number houses", "5 houses", true, "number");
        assertThatPatternMatchesStep(parser, "my house", "my house", true);
        assertThatPatternMatchesStep(parser, "I toggle the cell at ( %column , %row )",
                "I toggle the cell at ( 3 , 4 )", true, "column", "row");
        assertThatPatternMatchesStep(parser, "%name should ask, \"Why?\"", "Fred should ask, \"Why?\"", true, "name");
        assertThatPatternMatchesStep(parser, "%thousands x 10^3", "2 x 10^3", true, "thousands");
    }

    @Test
    void shouldMatchStepWithPatternsUsingCustomCharacterClass() {
        RegexPrefixCapturingPatternParser parserAllowingOnlyLettersInParameterNames = new RegexPrefixCapturingPatternParser("$", "[\\p{L}]");
        assertThatPatternMatchesStep(parserAllowingOnlyLettersInParameterNames, "a house with $numberOfFirstFloorDoors doors and $facing windows",
                "a house with 3 doors and 4 windows", true, "numberOfFirstFloorDoors", "facing");
        assertThatPatternMatchesStep(parserAllowingOnlyLettersInParameterNames, "a house with $numberOf1stFloorDoors doors and $facing2 windows",
                "a house with 3 doors and 4 windows", false);
    }

    @Test
    void shouldEscapeRegexPunctuationUsedInPatterns() {
        StepMatcher matcherWithAllTheRegexPunctuation = parser
                .parseStep(StepType.GIVEN, "$regexp should not be confused by []{}?^.*()+\\");
        Matcher matcher = matcherWithAllTheRegexPunctuation.matcher(
                "[]{}?^.*()+\\ should not be confused by []{}?^.*()+\\");
        assertThat(matcher.matches(), is(true));
        assertThat(matcher.group(1), equalTo("[]{}?^.*()+\\"));
    }
    
    @Test
    void shouldMatchStepWithPatternContainingRegexPunctuation() {
        assertThatPatternMatchesStep(parser, "a house with no. $number",
                "a house with no. 3", true, "number");
        assertThatPatternMatchesStep(parser, "a hotel with $number *",
                "a hotel with 3 *", true, "number");
    }

    private void assertThatPatternMatchesStep(StepPatternParser parser, String pattern, String step,
            boolean matching, String... parameterNames) {
        StepMatcher stepMatcher = parser.parseStep(StepType.GIVEN, pattern);
        assertThat(stepMatcher.matcher(step).matches(), is(matching));
        assertThat(stepMatcher.parameterNames(), equalTo(parameterNames));
    }

    @Test
    void shouldNotCareSoMuchAboutWhitespace() {
        StepMatcher stepMatcher = parser.parseStep(StepType.GIVEN, "The grid looks like $grid");

        // Given an argument on a new line
        Matcher matcher1 = stepMatcher.matcher("The grid looks like\n" + "..\n" + "..\n");
        assertThat(matcher1.matches(), is(true));
        assertThat(matcher1.group(1), equalTo("..\n" + "..\n"));

        // Given an argument on a new line with extra spaces
        Matcher matcher2 = stepMatcher.matcher("The grid looks like \n" + "..\n" + "..\n");
        assertThat(matcher2.matches(), is(true));
        assertThat(matcher2.group(1), equalTo("..\n" + "..\n"));

        // Given an argument with extra spaces
        Matcher matcher3 = stepMatcher.matcher("The grid looks like  .");
        assertThat(matcher3.matches(), is(true));
        assertThat(matcher3.group(1), equalTo("."));
    }

    @Test
    void shouldExtractParameterNamesFromStepPattern() {
        String[] names = parser.parseStep(StepType.GIVEN, "The grid $name looks like $grid").parameterNames();
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }
    
    @Test
    void shouldExtractParameterNamesWithoutQuotes() {
        String[] names = parser.parseStep(StepType.GIVEN, "The grid \"$name\" looks like \"$grid\"").parameterNames();
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }

}
