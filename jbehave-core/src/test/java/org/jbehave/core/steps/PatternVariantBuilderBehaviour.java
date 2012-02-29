package org.jbehave.core.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.jbehave.core.steps.PatternVariantBuilder;
import org.junit.Test;

public class PatternVariantBuilderBehaviour {
	
	@Test
	public void shouldReturnItselfForNoPatternString() {
		PatternVariantBuilder builder = new PatternVariantBuilder("No variants");
        assertEquals("No variants", builder.getInput());
		Set<String> variants = builder.allVariants();
        assertEquals("No variants", variants.iterator().next());
        assertEquals(1, variants.size());
	}

	@Test
	public void shouldReturnTwoVariantsForOnePattern() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {Two|One} variants");
        assertEquals("There are {Two|One} variants", builder.getInput());
        Set<String> result = builder.allVariants();
		assertEquals(2, result.size());
		assertTrue(result.contains("There are One variants"));
		assertTrue(result.contains("There are Two variants"));
	}

	@Test
	public void shouldReturnFourVariantsForTwoPatterns() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {Two|One} variants, {hooray|alas}!");
        Set<String> result = builder.allVariants();
		assertEquals(4, result.size());
		assertTrue(result.contains("There are One variants, hooray!"));
		assertTrue(result.contains("There are Two variants, hooray!"));
		assertTrue(result.contains("There are One variants, alas!"));
		assertTrue(result.contains("There are Two variants, alas!"));
	}

	@Test
	public void shouldReturnFourVariantsForTwoPatternsWithOptionElements() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {One|} variants{, hooray|}!");
        Set<String> result = builder.allVariants();
        assertEquals(4, result.size());
		assertTrue(result.contains("There are One variants, hooray!"));
		assertTrue(result.contains("There are  variants, hooray!"));
		assertTrue(result.contains("There are One variants!"));
		assertTrue(result.contains("There are  variants!"));
	}

	@Test
	public void shouldReturnFourVariantsForTwoPatternsWithOptionElementsWithWhitespaceCompression() {
		PatternVariantBuilder builder = new PatternVariantBuilder("There are {One|} variants{, hooray|}!");
		Set<String> result = builder.allVariants(true); // collapse whitespaces to 1
        assertEquals(4, result.size());
		assertTrue(result.contains("There are One variants, hooray!"));
		assertTrue(result.contains("There are variants, hooray!"));
		assertTrue(result.contains("There are One variants!"));
		assertTrue(result.contains("There are variants!"));
	}
	
	@Test
	public void shouldHandleSpecialCharacters() {
		PatternVariantBuilder builder = new PatternVariantBuilder("When $A {+|plus|is added to} $B");
		Set<String> result = builder.allVariants();
        assertEquals(3, result.size());
		assertTrue(result.contains("When $A + $B"));
		assertTrue(result.contains("When $A plus $B"));
		assertTrue(result.contains("When $A is added to $B"));
	}

}
