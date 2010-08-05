package org.jbehave.core.parsers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class RegexPrefixCapturingPatternParserBehaviour {

    private StepPatternParser parser = new RegexPrefixCapturingPatternParser();

    @Test
    public void shouldMatchStepWithPatterns() {
        assertThatPatternMatchesStep("a house with $numberOfDoors doors and $some windows", "a house with 3 doors and 4 windows", "numberOfDoors", "some");
        assertThatPatternMatchesStep("the house on $street", "the house on Easy Street", "street");
        assertThatPatternMatchesStep("$number houses", "5 houses", "number");
        assertThatPatternMatchesStep("my house", "my house");
        assertThatPatternMatchesStep("I toggle the cell at ( $column , $row )", "I toggle the cell at ( 3 , 4 )", "column", "row");
        assertThatPatternMatchesStep("$name should ask, \"Why?\"", "Fred should ask, \"Why?\"", "name");
        assertThatPatternMatchesStep("$thousands x 10^3", "2 x 10^3", "thousands");
    }
    
    @Test
    public void shouldEscapeExistingRegexPunctuationUsedInPatterns() {
        StepMatcher aMatcherWithAllTheRegexPunctuation = parser
            .parseStep("$regexp should not be confused by []{}?^.*()+\\");
        assertThat(aMatcherWithAllTheRegexPunctuation.matches("[]{}?^.*()+\\ should not be confused by []{}?^.*()+\\"), is(true));
        assertThat(aMatcherWithAllTheRegexPunctuation.parameter(1), equalTo("[]{}?^.*()+\\"));
    }
    
    private void assertThatPatternMatchesStep(String pattern, String step, String... parametersNames) {
        StepMatcher stepMatcher = parser.parseStep(pattern);
        assertThat(stepMatcher.matches(step), is(true));
        assertThat(stepMatcher.parameterNames(), equalTo(parametersNames));
    }

    @Test
    public void shouldNotCareSoMuchAboutWhitespace() {
        StepMatcher stepMatcher = parser.parseStep("The grid looks like $grid");

        // Given an argument on a new line
        assertThat(stepMatcher.matches(
        		"The grid looks like\n" +
                "..\n" +
                "..\n"), is(true));
        assertThat(stepMatcher.parameter(1), equalTo(
                "..\n" +
                "..\n"));
        
        // Given an argument on a new line with extra spaces
        assertThat(stepMatcher.matches(
        		"The grid looks like \n" +
                "..\n" +
                "..\n"), is(true));
        assertThat(stepMatcher.parameter(1), equalTo(
                "..\n" +
                "..\n"));
        
        // Given an argument with extra spaces
        assertThat(stepMatcher.matches("The grid looks like  ."), is(true));
        assertThat(stepMatcher.parameter(1), equalTo("."));
    }
    
    @Test
    public void shouldExtractParameterNamesFromStepPattern(){
        String[] names  = parser.parseStep("The grid $name looks like $grid").parameterNames();
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }

}
