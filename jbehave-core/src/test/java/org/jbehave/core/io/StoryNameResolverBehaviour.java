package org.jbehave.core.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

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

}
