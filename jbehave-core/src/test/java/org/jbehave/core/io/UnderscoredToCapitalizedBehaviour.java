package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class UnderscoredToCapitalizedBehaviour {

    @Test
    void shouldResolveNameWithDefaultExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized();
        assertThat(resolver.resolveName("some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some/path/some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some.package.some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some/story/path/some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some.story.package.some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("this.story/path/some_story.story"), equalTo("Some Story"));
    }

    @Test
    void shouldResolveNameWithCustomExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized(".foo");
        assertThat(resolver.resolveName("some_story.foo"), equalTo("Some Story"));
    }

    @Test
    void shouldResolveNameWithNoExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized("");
        assertThat(resolver.resolveName("com.blah.story.x_y_z_google_search"), equalTo("X Y Z Google Search"));
    }

}
