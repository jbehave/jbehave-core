package org.jbehave.core.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.core.io.UnderscoredCamelCaseResolver.NUMBERS_AS_UPPER_CASE_LETTERS_PATTERN;

import org.jbehave.core.junit.JUnitStory;
import org.junit.Test;

public class StoryPathResolverBehaviour {

    @Test
    public void shouldResolveClassNamePreservingCase() {
        StoryPathResolver resolver = new CasePreservingResolver();
        assertThat(resolver.resolve(CamelCase.class),
                equalTo("org/jbehave/core/io/CamelCase.story"));
    }

    @Test
    public void shouldResolveClassNamePreservingCaseWithNumbers() {
        StoryPathResolver resolver = new CasePreservingResolver(".story");
        assertThat(resolver.resolve(CamelCaseWithA3Qualifier.class),
                equalTo("org/jbehave/core/io/CamelCaseWithA3Qualifier.story"));
    }
    
	@Test
	public void shouldResolveCamelCasedClassNameToCasePreservingName() {
		StoryPathResolver resolver = new CasePreservingResolver("");
		assertThat(resolver.resolve(CamelCaseStory.class),
				equalTo("org/jbehave/core/io/CamelCaseStory"));
	}

	@Test
	public void shouldResolveCamelCasedClassNameToCasePreservingNameWithExtension() {
		StoryPathResolver resolver = new CasePreservingResolver(".story");
		assertThat(resolver.resolve(CamelCaseStory.class),
				equalTo("org/jbehave/core/io/CamelCaseStory.story"));
	}

	@Test
	public void shouldResolveCamelCasedClassNameToUnderscoredNameWithDefaultExtension() {
		StoryPathResolver resolver = new UnderscoredCamelCaseResolver();
		assertThat(resolver.resolve(CamelCase.class),
				equalTo("org/jbehave/core/io/camel_case.story"));
	}

	@Test
	public void shouldResolveCamelCasedClassNameToUnderscoredNameWithExtension() {
		StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
		assertThat(resolver.resolve(CamelCase.class),
				equalTo("org/jbehave/core/io/camel_case.story"));
	}

	/**
	 * Some teams are not going to have /stories/ directories, they are going to
	 * co-mingle with tests and match with *Story
	 */
	@Test
	public void shouldResolveCamelCasedClassNameToUnderscoredNameWithExtensionStrippingExtraneousWord() {
		StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story")
				.removeFromClassName("Story");
		assertThat(resolver.resolve(CamelCaseStory.class),
				equalTo("org/jbehave/core/io/camel_case.story"));
	}

	@Test
	public void shouldResolveCamelCasedClassNameWithNumbersTreatedAsLowerCaseLetters() {
		StoryPathResolver resolver = new UnderscoredCamelCaseResolver("");
		assertThat(resolver.resolve(CamelCaseWithA3Qualifier.class),
				equalTo("org/jbehave/core/io/camel_case_with_a3_qualifier"));
		assertThat(resolver.resolve(CamelCaseWithA33Qualifier.class),
				equalTo("org/jbehave/core/io/camel_case_with_a33_qualifier"));
	}

	@Test
	public void shouldResolveCamelCasedClassNameWithNumbersTreatedAsUpperCaseLetters() {
		StoryPathResolver resolver = new UnderscoredCamelCaseResolver("",
				NUMBERS_AS_UPPER_CASE_LETTERS_PATTERN);
		assertThat(resolver.resolve(CamelCaseWithA3Qualifier.class),
				equalTo("org/jbehave/core/io/camel_case_with_a_3_qualifier"));
		assertThat(resolver.resolve(CamelCaseWithA33Qualifier.class),
				equalTo("org/jbehave/core/io/camel_case_with_a_3_3_qualifier"));
	}
	
	static class CamelCaseStory extends JUnitStory {
		
	}

	static class CamelCase extends JUnitStory {

	}

	static class CamelCaseWithA3Qualifier extends JUnitStory {

	}

	static class CamelCaseWithA33Qualifier extends JUnitStory {

	}
}
