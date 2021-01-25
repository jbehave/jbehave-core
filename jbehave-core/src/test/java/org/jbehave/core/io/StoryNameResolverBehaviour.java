package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

public class StoryNameResolverBehaviour {

    @Test
    public void shouldResolveUnderscoredToCapitalized() {
        StoryNameResolver resolver = new UnderscoredToCapitalized();
        assertThat(resolver.resolveName("org/jbehave/core/io/camel_case.story"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("/org/jbehave/core/io/camel_case.story"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("/camel_case.story"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("camel_case.story"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("org.jbehave.core.io.camel_case"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("camel_case"), equalTo("Camel Case"));
    }

    @Test
    public void shouldResolveUnderscoredToCapitalizedWithCustomExtension() {
        StoryNameResolver resolver = new UnderscoredToCapitalized(".ext");
        assertThat(resolver.resolveName("org/jbehave/core/io/camel_case.ext"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("/org/jbehave/core/io/camel_case.ext"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("/camel_case.ext"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("camel_case.ext"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("org.jbehave.core.io.camel_case"), equalTo("Camel Case"));
        assertThat(resolver.resolveName("camel_case"), equalTo("Camel Case"));
    }

    @Test
    public void shouldResolveAncestorWithDefaultAncestors() {
        StoryNameResolver resolver = new AncestorDelegatingResolver();
        assertThat(resolver.resolveName("org/jbehave/core/io/camel_case.story"), equalTo("Io Camel Case"));
    }

    @Test
    public void shouldResolveAncestorWithCustomAncestors() {
        StoryNameResolver resolver = new AncestorDelegatingResolver(2);
        assertThat(resolver.resolveName("org/jbehave/core/io/camel_case.story"), equalTo("Core Io Camel Case"));
    }

    @Test
    public void shouldResolveAncestorWithCustomDelegate() {
        StoryNameResolver delegate = mock(StoryNameResolver.class);
		when(delegate.resolveName("io")).thenReturn("IO");
		when(delegate.resolveName("camel_case.story")).thenReturn("CC");
		StoryNameResolver resolver = new AncestorDelegatingResolver(1, delegate);
        assertThat(resolver.resolveName("org/jbehave/core/io/camel_case.story"), equalTo("IO CC"));
    }

}
