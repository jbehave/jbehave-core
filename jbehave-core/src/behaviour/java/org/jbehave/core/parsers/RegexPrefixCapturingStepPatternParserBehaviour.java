package org.jbehave.core.parsers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class RegexPrefixCapturingStepPatternParserBehaviour {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void shouldReplaceAllDollarArgumentsWithCaptures() {
        StepPatternParser parser = new RegexPrefixCapturingPatternParser();
        assertThat(parser.parseStep("a house with $numberOfDoors doors and $some windows").matches("a house with 3 doors and 4 windows"), is(true));
        assertThat(parser.parseStep("the house on $street").matches("the house on Easy Street"), is(true));
        assertThat(parser.parseStep("$number houses").matches("5 houses"), is(true));
        assertThat(parser.parseStep("my house").matches("my house"), is(true));
    }
    
    @Test
    public void shouldEscapeExistingPunctuationUsedInRegexps() {
        StepPatternParser parser = new RegexPrefixCapturingPatternParser();
        assertThat(parser.parseStep("I toggle the cell at ($column, $row)").matches("I toggle the cell at (3, 4)"), is(true));
        assertThat(parser.parseStep("$name should ask, \"Why?\"").matches("Fred should ask, \"Why?\""), is(true));
        assertThat(parser.parseStep("$thousands x 10^3").matches("2 x 10^3"), is(true));
        
        StepMatcher aMatcherWithAllTheRegexpPunctuation = parser
            .parseStep("$regexp should not be confused by []{}?^.*()+\\");
        assertThat(aMatcherWithAllTheRegexpPunctuation.matches("[]{}?^.*()+\\ should not be confused by []{}?^.*()+\\"), is(true));
        assertThat(aMatcherWithAllTheRegexpPunctuation.parameter(1), equalTo("[]{}?^.*()+\\"));
    }
    
    @Test
    public void shouldNotCareSoMuchAboutWhitespace() {
        StepPatternParser parser = new RegexPrefixCapturingPatternParser();
        StepMatcher stepMatcher = parser.parseStep("The grid looks like $grid");

        // Given an argument on a new line
        assertThat(stepMatcher.matches(
        		"The grid looks like" + NL +
                ".." + NL +
                ".." + NL), is(true));
        assertThat(stepMatcher.parameter(1), equalTo(
                ".." + NL +
                ".." + NL));
        
        // Given an argument on a new line with extra spaces
        assertThat(stepMatcher.matches(
        		"The grid looks like " + NL +
                ".." + NL +
                ".." + NL), is(true));
        assertThat(stepMatcher.parameter(1), equalTo(
                ".." + NL +
                ".." + NL));
        
        // Given an argument with extra spaces
        assertThat(stepMatcher.matches("The grid looks like  ."), is(true));
        assertThat(stepMatcher.parameter(1), equalTo(
                "."));        
    }
    
    @Test
    public void shouldExtractParameterNamesFromStepPattern(){
    	StepPatternParser parser = new RegexPrefixCapturingPatternParser();
        String[] names  = parser.parseStep("The grid $name looks like $grid").parameterNames();
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }

}
