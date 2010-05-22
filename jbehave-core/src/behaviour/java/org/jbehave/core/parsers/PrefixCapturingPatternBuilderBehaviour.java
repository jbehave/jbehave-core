package org.jbehave.core.parsers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.parsers.PrefixCapturingPatternBuilder;
import org.jbehave.core.parsers.StepPatternBuilder;
import org.junit.Test;


public class PrefixCapturingPatternBuilderBehaviour {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void shouldReplaceAllDollarArgumentsWithCaptures() {
        StepPatternBuilder builder = new PrefixCapturingPatternBuilder();
        assertThat(builder.buildPattern("a house with $numberOfDoors doors and $some windows").matcher("a house with 3 doors and 4 windows").matches(), is(true));
        assertThat(builder.buildPattern("the house on $street").matcher("the house on Easy Street").matches(), is(true));
        assertThat(builder.buildPattern("$number houses").matcher("5 houses").matches(), is(true));
        assertThat(builder.buildPattern("my house").matcher("my house").matches(), is(true));
    }
    
    @Test
    public void shouldEscapeExistingPunctuationUsedInRegexps() {
        StepPatternBuilder builder = new PrefixCapturingPatternBuilder();
        assertThat(builder.buildPattern("I toggle the cell at ($column, $row)").matcher("I toggle the cell at (3, 4)").matches(), is(true));
        assertThat(builder.buildPattern("$name should ask, \"Why?\"").matcher("Fred should ask, \"Why?\"").matches(), is(true));
        assertThat(builder.buildPattern("$thousands x 10^3").matcher("2 x 10^3").matches(), is(true));
        
        Matcher aMatcherWithAllTheRegexpPunctuation = builder
            .buildPattern("$regexp should not be confused by []{}?^.*()+\\")
            .matcher("[]{}?^.*()+\\ should not be confused by []{}?^.*()+\\");
        assertThat(aMatcherWithAllTheRegexpPunctuation.matches(), is(true));
        assertThat(aMatcherWithAllTheRegexpPunctuation.group(1), equalTo("[]{}?^.*()+\\"));
    }
    
    @Test
    public void shouldNotCareSoMuchAboutWhitespace() {
        StepPatternBuilder matcher = new PrefixCapturingPatternBuilder();
        Pattern pattern = matcher.buildPattern("The grid looks like $grid");
        
        // Given an argument on a new line
        Matcher matched = pattern.matcher(
                "The grid looks like" + NL +
                ".." + NL +
                ".." + NL
                );
        assertThat(matched.matches(), is(true));
        assertThat(matched.group(1), equalTo(
                ".." + NL +
                ".." + NL));
        
        // Given an argument on a new line with extra spaces
        matched = pattern.matcher(
                "The grid looks like " + NL +
                ".." + NL +
                ".." + NL
                );
        assertThat(matched.matches(), is(true));
        assertThat(matched.group(1), equalTo(
                ".." + NL +
                ".." + NL));
        
        // Given an argument with extra spaces
        matched = pattern.matcher(
                "The grid looks like  .");
        assertThat(matched.matches(), is(true));
        assertThat(matched.group(1), equalTo(
                "."));        
    }
    
    @Test
    public void shouldExtractParameterNamesFromStepPattern(){
    	StepPatternBuilder builder = new PrefixCapturingPatternBuilder();
        String[] names  = builder.extractGroupNames("The grid $name looks like $grid");
        assertThat(names.length, equalTo(2));
        assertThat(names[0], equalTo("name"));
        assertThat(names[1], equalTo("grid"));
    }

}
