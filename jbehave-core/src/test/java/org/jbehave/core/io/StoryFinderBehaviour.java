package org.jbehave.core.io;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class StoryFinderBehaviour {

    private StoryFinder finder = new StoryFinder();

    @Test
    public void shouldFindNoPaths() {
        List<String> storyPaths = new ArrayList<>(finder.findPaths("src/test/java", (String) null, null));
        assertThat(storyPaths.size(), equalTo(0));
    }

    @Test
    public void shouldFindPathsWithFiltersAsArrays() {
        List<String> storyPaths = new ArrayList<>(
                finder.findPaths("src/test/java", new String[] { "**/stories/*_story" }, new String[] {}));
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsWithFiltersAsCSV() {
        List<String> storyPaths = new ArrayList<>(
                finder.findPaths("src/test/java", "**/stories/*_story,**/my_*", "**/*.txt,**/none*"));
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsAndPrefixThem() {
        List<String> storyPaths = new ArrayList<>(
                finder.findPaths("src/test/java", asList("**/stories/*_story"), asList(""), "file:"));
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("file:org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsAndIgnorePrefixIfBlank() {
        List<String> storyPaths = new ArrayList<>(
                finder.findPaths("src/test/java", asList("**/stories/*_story"), asList(""), ""));
        assertThat(storyPaths.size(), equalTo(4));
        assertThat(storyPaths, hasItem(containsString("/")));
        assertThat(storyPaths, hasItem(not(startsWith("/"))));
        assertThat(storyPaths, hasItem(startsWith("org/jbehave/core/io/stories")));
        assertThat(storyPaths, hasItem(endsWith("_story")));
    }

    @Test
    public void shouldFindPathsFromJarPath() {
        String jarPath = "src/test/resources/stories.jar";
        assertThat(finder.findPaths(jarPath, "**/*.story", "**/*_search.story"),
                equalTo((asList("etsy_browse.story", "etsy_cart.story"))));
    }

    @Test
    public void shouldFindPathsFromJarURL() {
        URL jarURL = CodeLocations.codeLocationFromPath("src/test/resources/stories.jar");
        assertThat(finder.findPaths(jarURL, "**/*.story", "**/*_search.story"),
                equalTo((asList("etsy_browse.story", "etsy_cart.story"))));
    }

    @Test
    public void shouldFindClassNamesAndTrasformThemIfMatchingExtension() {
        List<String> classNames = new ArrayList<>(
                finder.findClassNames("src/test/java", asList("**/stories/*.java"), asList("")));
        assertThat(classNames.size(), equalTo(3));
        assertThat(classNames, hasItem(not(containsString("/"))));
        assertThat(classNames, hasItem(not(endsWith(".java"))));
        assertThat(classNames, hasItem(startsWith("org.jbehave.core.io.stories")));
    }

    @Test
    public void shouldFindClassNamesButNotTransformThemIfNotMatchingExtension() {
        List<String> classNames = new ArrayList<>(
                finder.findClassNames("src/test/java", asList("**/stories/*.groovy"), asList("")));
        assertThat(classNames.size(), equalTo(1));
        assertThat(classNames, hasItem(containsString("/")));
        assertThat(classNames, hasItem(endsWith(".groovy")));
        assertThat(classNames, hasItem(startsWith("org/jbehave/core/io/stories")));
    }

    @Test
    public void shouldFindAndSortClassNamesWithCustomComparator() {
        // comparator that sorts in reversed natural order
        final Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return -1 * o1.compareTo(o2);
            }
        };
        finder = new StoryFinder(comparator);
        List<String> classNames = finder.findClassNames("src/test/java", asList("**/stories/*.java"), asList(""));
        List<String> sorted = new ArrayList<>(classNames);
        Collections.sort(sorted, comparator);
        assertThat(classNames.toString(), equalTo(sorted.toString()));
    }

    @Test
    public void shouldFindClassNamesAndTrasformThemIfMatchingCustomExtension() {
        finder = new StoryFinder(".groovy");
        List<String> classNames = new ArrayList<>(
                finder.findClassNames("src/test/java", asList("**/stories/*.groovy"), asList("")));
        assertThat(classNames.size(), equalTo(1));
        assertThat(classNames, hasItem(not(containsString("/"))));
        assertThat(classNames, hasItem(not(endsWith(".groovy"))));
        assertThat(classNames, hasItem(startsWith("org.jbehave.core.io.stories")));
    }

    @Test
    public void shouldNormalisePaths() {
        assertThat(finder.normalise(asList("path/to/a.story", "/path/to/a.story")),
                equalTo(asList("path/to/a.story", "/path/to/a.story")));
        assertThat(finder.normalise(asList("path\\to\\a.story", "\\path\\to\\a.story")),
                equalTo(asList("path/to/a.story", "/path/to/a.story")));
    }

    @Test
    public void shouldIgnoreNullFiltersWhenFindingPaths() {
        assertThat(finder.findPaths("src/test/java", (List<String>) null, null).size(), greaterThan(0));
    }

    @Test
    public void shouldReturnEmptyListForInexistentBasedir() {
        assertThat(finder.findPaths("/inexistent", asList(""), asList("")).size(), equalTo(0));
    }

}
