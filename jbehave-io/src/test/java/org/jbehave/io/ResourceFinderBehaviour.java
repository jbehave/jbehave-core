package org.jbehave.io;

import org.jbehave.io.ResourceFinder.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ResourceFinderBehaviour {

    @Test
    public void canFindResourceInClasspath() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("org/jbehave/io/resource.txt"), is("A test resource"));
    }

    @Test
    public void canFindResourceFromRootDirectoryInClasspath() throws IOException {
        ResourceFinder finder = new ResourceFinder("classpath:org/jbehave");
        assertThat(finder.resourceAsString("io/resource.txt"), is("A test resource"));
    }

    @Test
    public void canChangeRootDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        finder.useRootDirectory("classpath:org/jbehave/io");
        assertThat(finder.resourceAsString("resource.txt"), is("A test resource"));
    }

    @Test
    public void canFindResourceInFilesystem() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("src/test/java/org/jbehave/io/resource.txt"), is("A test resource"));
    }

    @Test
    public void canFindResourceFromRootDirectoryInFilesystem() throws IOException {
        ResourceFinder finder = new ResourceFinder("src/test/java/org/jbehave");
        assertThat(finder.resourceAsString("io/resource.txt"), is("A test resource"));
    }

    @Test
    public void canFindResourceInJarsInClasspath() {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("ftl/jbehave-reports.ftl").length(), greaterThan(0));
    }

    @Test
    public void cannotFindResourceFromInexistentClasspathDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder("classpath:inexistent");
        try {
            finder.resourceAsString("resource.txt");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(ResourceNotFoundException.class)));
        }
    }

    @Test
    public void cannotFindResourceFromInexistentFileDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder("/inexistent");
        try {
            finder.resourceAsString("resource.txt");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(ResourceNotFoundException.class)));
        }
    }

}