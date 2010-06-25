package org.jbehave.core.io;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import org.junit.Test;

public class StoryPathFinderBehaviour {

    @Test
    public void shouldFindPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        List<String> storyPaths = finder.findPaths("src/behaviour/java", asList("**/stories/*_story"), asList(""));
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsAndPrefixThem() {
        StoryPathFinder finder = new StoryPathFinder();
        List<String> storyPaths = finder.findPaths("src/behaviour/java", asList("**/stories/*_story"), asList(""), "file:");
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("file:org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsAndTransformThemToClassNames() {
        StoryPathFinder finder = new StoryPathFinder();
        List<String> storyPaths = finder.findPaths("src/behaviour/java", asList("**/stories/*.java"), asList(""));
        List<String> classNames = new PathToClassNames().transform(storyPaths);
        assertThat(classNames.size(), equalTo(2));
        assertThat(classNames, hasItem(not(containsString("/"))));
        assertThat(classNames, hasItem(not(endsWith(".java"))));
        assertThat(classNames, hasItem(startsWith("org.jbehave.core.io.stories")));
    }

    @Test
    public void shouldNormalisePaths(){
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.normalise(asList("path/to/a.story", "/path/to/a.story")), equalTo(asList("path/to/a.story", "/path/to/a.story")));
        assertThat(finder.normalise(asList("path\\to\\a.story", "\\path\\to\\a.story")), equalTo(asList("path/to/a.story", "/path/to/a.story")));
    }
    
    @Test
    public void shouldIgnoreNullFiltersWhenFindingPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.findPaths("src/behaviour/java", null, null).size(), greaterThan(0));
    }

    @Test
    public void shouldReturnEmptyListForInexistentBasedir() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.findPaths("/inexistent", asList(""), asList("")).size(), equalTo(0));
    }
    
}
