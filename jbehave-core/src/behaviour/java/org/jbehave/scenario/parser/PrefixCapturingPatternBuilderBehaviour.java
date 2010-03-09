package org.jbehave.scenario.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;


public class PrefixCapturingPatternBuilderBehaviour {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void shouldReplaceAllDollarArgumentsWithCaptures() {
        StepPatternBuilder builder = new PrefixCapturingPatternBuilder();
        ensureThat(builder.buildPattern("a house with $numberOfDoors doors and $some windows").matcher("a house with 3 doors and 4 windows").matches());
        ensureThat(builder.buildPattern("the house on $street").matcher("the house on Easy Street").matches());
        ensureThat(builder.buildPattern("$number houses").matcher("5 houses").matches());
        ensureThat(builder.buildPattern("my house").matcher("my house").matches());
    }
    
    @Test
    public void shouldEscapeExistingPunctuationUsedInRegexps() {
        StepPatternBuilder builder = new PrefixCapturingPatternBuilder();
        ensureThat(builder.buildPattern("I toggle the cell at ($column, $row)").matcher("I toggle the cell at (3, 4)").matches());
        ensureThat(builder.buildPattern("$name should ask, \"Why?\"").matcher("Fred should ask, \"Why?\"").matches());
        ensureThat(builder.buildPattern("$thousands x 10^3").matcher("2 x 10^3").matches());
        
        Matcher aMatcherWithAllTheRegexpPunctuation = builder
            .buildPattern("$regexp should not be confused by []{}?^.*()+\\")
            .matcher("[]{}?^.*()+\\ should not be confused by []{}?^.*()+\\");
        ensureThat(aMatcherWithAllTheRegexpPunctuation.matches());
        ensureThat(aMatcherWithAllTheRegexpPunctuation.group(1), equalTo("[]{}?^.*()+\\"));
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
        ensureThat(matched.matches());
        ensureThat(matched.group(1), equalTo(
                ".." + NL +
                ".." + NL));
        
        // Given an argument on a new line with extra spaces
        matched = pattern.matcher(
                "The grid looks like " + NL +
                ".." + NL +
                ".." + NL
                );
        ensureThat(matched.matches());
        ensureThat(matched.group(1), equalTo(
                ".." + NL +
                ".." + NL));
        
        // Given an argument with extra spaces
        matched = pattern.matcher(
                "The grid looks like  .");
        ensureThat(matched.matches());
        ensureThat(matched.group(1), equalTo(
                "."));        
    }
    
    @Test
    public void shouldExtractParameterNamesFromStepPattern(){
    	StepPatternBuilder builder = new PrefixCapturingPatternBuilder();
        String[] names  = builder.extractGroupNames("The grid $name looks like $grid");
        ensureThat(names.length, equalTo(2));
        ensureThat(names[0], equalTo("name"));
        ensureThat(names[1], equalTo("grid"));
    }

}
