package org.jbehave.core.steps;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class PatternVariantBuilderBehaviour {
	
	@Test
	public void shouldReturnItselfForNoPatternString() {
		PatternVariantBuilder builder = new PatternVariantBuilder("No variants");
        assertThat(builder.getInput(), equalTo("No variants"));
        Set<String> variants = builder.allVariants();
        assertThat(variants.iterator().next(), equalTo("No variants"));
        assertThat(variants.size(), equalTo(1));
    }

	@Test
	public void shouldReturnTwoVariantsForOnePattern() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {Two|One} variants");
        assertThat(builder.getInput(), equalTo("There are {Two|One} variants"));
        Set<String> result = builder.allVariants();
        assertThat(result.size(), equalTo(2));
        assertThat(result.contains("There are One variants"), is(true));
        assertThat(result.contains("There are Two variants"), is(true));
    }

	@Test
	public void shouldReturnFourVariantsForTwoPatterns() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {Two|One} variants, {hooray|alas}!");
        Set<String> result = builder.allVariants();
        assertThat(result.size(), equalTo(4));
        assertThat(result.contains("There are One variants, hooray!"), is(true));
        assertThat(result.contains("There are Two variants, hooray!"), is(true));
        assertThat(result.contains("There are One variants, alas!"), is(true));
        assertThat(result.contains("There are Two variants, alas!"), is(true));
    }

	@Test
	public void shouldReturnFourVariantsForTwoPatternsWithOptionElements() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {One|} variants{, hooray|}!");
        Set<String> result = builder.allVariants();
        assertThat(result.size(), equalTo(4));
        assertThat(result.contains("There are One variants, hooray!"), is(true));
        assertThat(result.contains("There are  variants, hooray!"), is(true));
        assertThat(result.contains("There are One variants!"), is(true));
        assertThat(result.contains("There are  variants!"), is(true));
    }

	@Test
	public void shouldReturnFourVariantsForTwoPatternsWithOptionElementsWithWhitespaceCompression() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {One|} variants{, hooray|}!");
		Set<String> result = builder.allVariants(true); // collapse whitespaces to 1
        assertThat(result.size(), equalTo(4));
        assertThat(result.contains("There are One variants, hooray!"), is(true));
        assertThat(result.contains("There are variants, hooray!"), is(true));
        assertThat(result.contains("There are One variants!"), is(true));
        assertThat(result.contains("There are variants!"), is(true));
    }
	
	@Test
	public void shouldHandleSpecialCharacters() {
		PatternVariantBuilder builder = new PatternVariantBuilder("When $A {+|plus|is added to} $B");
		Set<String> result = builder.allVariants();
        assertThat(result.size(), equalTo(3));
        assertThat(result.contains("When $A + $B"), is(true));
        assertThat(result.contains("When $A plus $B"), is(true));
        assertThat(result.contains("When $A is added to $B"), is(true));
    }

	@Test
	public void hasUnclosedBracket() {
		PatternVariantBuilder builder = new PatternVariantBuilder("When $A {+|plus|is added to $B");
		Set<String> result = builder.allVariants();
        assertThat(result.size(), equalTo(1));
        assertThat(result.contains("When $A {+|plus|is added to $B"), is(true));
    }

	@Test
	public void hasUnclosedBrackets() {
        PatternVariantBuilder builder = new PatternVariantBuilder("When $A {+|plus|is added to} $B and }{$C");
        Set<String> result = builder.allVariants();
        assertThat(result.size(), equalTo(3));
        assertThat(result.contains("When $A + $B and }{$C"), is(true));
        assertThat(result.contains("When $A plus $B and }{$C"), is(true));
        assertThat(result.contains("When $A is added to $B and }{$C"), is(true));
    }

}
