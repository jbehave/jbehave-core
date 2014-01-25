package org.jbehave.core.parsers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.jbehave.core.steps.StepType;
import org.junit.Test;

public class RegexPrefixCapturingPatternParserBehaviour {

    private StepPatternParser parser = new RegexPrefixCapturingPatternParser();

    @Test
    public void shouldMatchStepWithPatterns() {
        assertThatPatternMatchesStep(parser, "a house with $numberOfDoors doors and $some windows",
                "a house with 3 doors and 4 windows", "numberOfDoors", "some");
        assertThatPatternMatchesStep(parser, "the house on $street", "the house on Easy Street", "street");
        assertThatPatternMatchesStep(parser, "$number houses", "5 houses", "number");
        assertThatPatternMatchesStep(parser, "my house", "my house");
        assertThatPatternMatchesStep(parser, "I toggle the cell at ( $column , $row )",
                "I toggle the cell at ( 3 , 4 )", "column", "row");
        assertThatPatternMatchesStep(parser, "$name should ask, \"Why?\"", "Fred should ask, \"Why?\"", "name");
        assertThatPatternMatchesStep(parser, "$thousands x 10^3", "2 x 10^3", "thousands");
    }

    @Test
    public void shouldMatchStepWithPatternsUsingNumbersInPlacehoderNames() {
        assertThatPatternMatchesStep(parser, "a house with $numberOf1stFloorDoors doors and $facing2 windows",
                "a house with 3 doors and 4 windows", "numberOf1stFloorDoors", "facing2");
    }

    @Test
    public void shouldMatchStepWithPatternsUsingAccentsInPlacehoderNames() {
        assertThatPatternMatchesStep(parser, "une maison avec $numérosDesPortes portes et $quelques fenêtres",
                "une maison avec 3 portes et 4 fenêtres", "numérosDesPortes", "quelques");
        assertThatPatternMatchesStep(parser, "ein Haus mit $anzahlDerTüren Türen und $einige Fenster",
                "ein Haus mit 3 Türen und 4 Fenster", "anzahlDerTüren", "einige");        
    }

    @Test
    public void shouldMatchStepWithPatternsOfCustomPrefix() {
        StepPatternParser parser = new RegexPrefixCapturingPatternParser("%");
        assertThat(((RegexPrefixCapturingPatternParser) parser).getPrefix(), equalTo("%"));
        assertThat(parser.toString(), containsString("prefix=%"));
        assertThatPatternMatchesStep(parser, "a house with %numberOfDoors doors and %some windows",
                "a house with 3 doors and 4 windows", "numberOfDoors", "some");
        assertThatPatternMatchesStep(parser, "the house on %street", "the house on Easy Street", "street");
        assertThatPatternMatchesStep(parser, "%number houses", "5 houses", "number");
        assertThatPatternMatchesStep(parser, "my house", "my house");
        assertThatPatternMatchesStep(parser, "I toggle the cell at ( %column , %row )",
                "I toggle the cell at ( 3 , 4 )", "column", "row");
        assertThatPatternMatchesStep(parser, "%name should ask, \"Why?\"", "Fred should ask, \"Why?\"", "name");
        assertThatPatternMatchesStep(parser, "%thousands x 10^3", "2 x 10^3", "thousands");
    }

    @Test
    public void shouldEscapeExistingRegexPunctuationUsedInPatterns() {
        StepMatcher aMatcherWithAllTheRegexPunctuation = parser
                .parseStep(StepType.GIVEN, "$regexp should not be confused by []{}?^.*()+\\");
        assertThat(aMatcherWithAllTheRegexPunctuation.matches("[]{}?^.*()+\\ should not be confused by []{}?^.*()+\\"),
                is(true));
        assertThat(aMatcherWithAllTheRegexPunctuation.parameter(1), equalTo("[]{}?^.*()+\\"));
    }

    private void assertThatPatternMatchesStep(StepPatternParser parser, String pattern, String step,
            String... parametersNames) {
        StepMatcher stepMatcher = parser.parseStep(StepType.GIVEN, pattern);
        assertThat(stepMatcher.matches(step), is(true));
        assertThat(stepMatcher.parameterNames(), equalTo(parametersNames));
    }

    @Test
    public void shouldNotCareSoMuchAboutWhitespace() {
        StepMatcher stepMatcher = parser.parseStep(StepType.GIVEN, "The grid looks like $grid");

        // Given an argument on a new line
        assertThat(stepMatcher.matches("The grid looks like\n" + "..\n" + "..\n"), is(true));
        assertThat(stepMatcher.parameter(1), equalTo("..\n" + "..\n"));

        // Given an argument on a new line with extra spaces
        assertThat(stepMatcher.matches("The grid looks like \n" + "..\n" + "..\n"), is(true));
        assertThat(stepMatcher.parameter(1), equalTo("..\n" + "..\n"));

        // Given an argument with extra spaces
        assertThat(stepMatcher.matches("The grid looks like  ."), is(true));
        assertThat(stepMatcher.parameter(1), equalTo("."));
    }

    @Test
    public void shouldExtractParameterNamesFromStepPattern() {
        String[] names = parser.parseStep(StepType.GIVEN, "The grid $name looks like $grid").parameterNames();
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }
    
    @Test
    public void shouldExtractParameterNamesWithoutQuotes() {
        String[] names = parser.parseStep(StepType.GIVEN, "The grid \"$name\" looks like \"$grid\"").parameterNames();
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }

}
