package org.jbehave.core.io;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UnderscoredToCapitalizedBehaviour {

    @Test
    public void shouldResolveNameWithDefaultExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized();
        assertThat(resolver.resolveName("some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some/path/some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some.package.some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some/story/path/some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("some.story.package.some_story.story"), equalTo("Some Story"));
        assertThat(resolver.resolveName("this.story/path/some_story.story"), equalTo("Some Story"));
    }

    @Test
    public void shouldResolveNameWithCustomExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized(".foo");
        assertThat(resolver.resolveName("some_story.foo"), equalTo("Some Story"));
    }

    @Test
    public void shouldResolveNameWithNoExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized("");
        assertThat(resolver.resolveName("com.blah.story.x_y_z_google_search"), equalTo("X Y Z Google Search"));
    }

}
