package org.jbehave.core.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnderscoredToCapitalizedBehaviour {

    @Test
    public void shouldResolveNameWithDefaultExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized();
        assertEquals("Some Story", resolver.resolveName("some_story.story"));
        assertEquals("Some Story", resolver.resolveName("some/path/some_story.story"));
        assertEquals("Some Story", resolver.resolveName("some.package.some_story.story"));
        assertEquals("Some Story", resolver.resolveName("some/story/path/some_story.story"));
        assertEquals("Some Story", resolver.resolveName("some.story.package.some_story.story"));
        assertEquals("Some Story", resolver.resolveName("this.story/path/some_story.story"));
    }

    @Test
    public void shouldResolveNameWithCustomExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized(".foo");
        assertEquals("Some Story", resolver.resolveName("some_story.foo"));
    }

    @Test
    public void shouldResolveNameWithNoExtension() {
        final StoryNameResolver resolver = new UnderscoredToCapitalized("");
        assertEquals("X Y Z Google Search", resolver.resolveName("com.blah.story.x_y_z_google_search"));
    }

}
