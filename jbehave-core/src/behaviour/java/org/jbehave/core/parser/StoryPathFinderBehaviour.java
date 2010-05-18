package org.jbehave.core.parser;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.io.InputStream;

import org.jbehave.core.errors.InvalidStoryPathException;
import org.junit.Ignore;
import org.junit.Test;

public class StoryPathFinderBehaviour {

    @Test
    public void shouldListPaths() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.listStoryPaths(".", ".", asList("**/stories/*.java"), asList("")).size(), greaterThan(0));
    }

    @Test
    public void shouldeturnEmptyListForInexistentBasedir() {
        StoryPathFinder finder = new StoryPathFinder();
        assertThat(finder.listStoryPaths("/inexistent", null, asList(""), asList("")).size(), equalTo(0));
    }

    @Test(expected= InvalidStoryPathException.class)
    @Ignore("Need to find alternative way to express invalid java path")
    public void shouldNotListPathsForJavaThatAreInvalid() {
        StoryPathFinder finder = new StoryPathFinder();
        finder.listStoryPaths(".", null, null, null);
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
