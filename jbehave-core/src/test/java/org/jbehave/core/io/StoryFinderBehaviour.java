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

public class StoryFinderBehaviour {

    private StoryFinder finder = new StoryFinder();
    
    @Test
    public void shouldFindPaths() {
        List<String> storyPaths = finder.findPaths("src/test/java", asList("**/stories/*_story"), asList(""));
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsAndPrefixThem() {
        List<String> storyPaths = finder.findPaths("src/test/java", asList("**/stories/*_story"), asList(""), "file:");
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("file:org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsAndIgnorePrefixIfBlank() {
        List<String> storyPaths = finder.findPaths("src/test/java", asList("**/stories/*_story"), asList(""), "");
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindClassNamesAndTrasformThemIfMatchingExtension() {
        List<String> classNames = finder.findClassNames("src/test/java", asList("**/stories/*.java"), asList(""));
        assertThat(classNames.size(), equalTo(3));
        assertThat(classNames, hasItem(not(containsString("/"))));
        assertThat(classNames, hasItem(not(endsWith(".java"))));
        assertThat(classNames, hasItem(startsWith("org.jbehave.core.io.stories")));
    }

    @Test
    public void shouldFindClassNamesButNotTransformThemIfNotMatchingExtension() {
        List<String> classNames = finder.findClassNames("src/test/java", asList("**/stories/*.groovy"), asList(""));
        assertThat(classNames.size(), equalTo(1));
        assertThat(classNames, hasItem(containsString("/")));
        assertThat(classNames, hasItem(endsWith(".groovy")));
        assertThat(classNames, hasItem(startsWith("org/jbehave/core/io/stories")));
    }
        
    @Test
    public void shouldFindClassNamesAndTrasformThemIfMatchingCustomExtension() {
        finder = new StoryFinder(".groovy");
        List<String> classNames = finder.findClassNames("src/test/java", asList("**/stories/*.groovy"), asList(""));
        assertThat(classNames.size(), equalTo(1));
        assertThat(classNames, hasItem(not(containsString("/"))));
        assertThat(classNames, hasItem(not(endsWith(".groovy"))));
        assertThat(classNames, hasItem(startsWith("org.jbehave.core.io.stories")));
    }

    @Test
    public void shouldNormalisePaths(){
        assertThat(finder.normalise(asList("path/to/a.story", "/path/to/a.story")), equalTo(asList("path/to/a.story", "/path/to/a.story")));
        assertThat(finder.normalise(asList("path\\to\\a.story", "\\path\\to\\a.story")), equalTo(asList("path/to/a.story", "/path/to/a.story")));
    }
    
    @Test
    public void shouldIgnoreNullFiltersWhenFindingPaths() {
        assertThat(finder.findPaths("src/test/java", null, null).size(), greaterThan(0));
    }

    @Test
    public void shouldReturnEmptyListForInexistentBasedir() {
        assertThat(finder.findPaths("/inexistent", asList(""), asList("")).size(), equalTo(0));
    }
    
}
