package org.jbehave.core.io;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class StoryPathFinderBehaviour {

    @Test
    public void shouldFindPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.listStoryPaths(".", ".", asList("src/**/stories/*.java"), asList("")).size(), equalTo(2));
        assertThat(finder.listStoryPaths(".", ".", asList("src/**/stories/*_story"), asList("")).size(), equalTo(4));
    }
    
    @Test
    public void shouldIgnoreNullFiltersWhenFindingPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.listStoryPaths(".", null, asList("src/**/stories/*.java"), null).size(), equalTo(2));
        assertThat(finder.listStoryPaths(".", null, null, null).size(), equalTo(2));
    }


    @Test
    public void shouldReturnEmptyListForInexistentBasedir() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.listStoryPaths("/inexistent", null, asList(""), asList("")).size(), equalTo(0));
    }
    
   static class InvalidClassLoader extends ClassLoader {

        @Override
        public InputStream getResourceAsStream(String name) {
            return new InputStream() {

                public int available() throws IOException {
                    return 1;
                }

                @Override
                public int read() throws IOException {
                    throw new IOException("invalid");
                }

            };
        }

    }

}
