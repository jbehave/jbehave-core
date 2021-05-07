package org.jbehave.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.jbehave.io.ResourceFinder.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ResourceFinderBehaviour {

    @Test
    void canFindResourceInClasspath() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("org/jbehave/io/resource.txt"), is("A test resource"));
    }

    @Test
    void canFindResourceFromRootDirectoryInClasspath() throws IOException {
        ResourceFinder finder = new ResourceFinder("classpath:org/jbehave");
        assertThat(finder.resourceAsString("io/resource.txt"), is("A test resource"));
    }

    @Test
    void canChangeRootDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        finder.useRootDirectory("classpath:org/jbehave/io");
        assertThat(finder.resourceAsString("resource.txt"), is("A test resource"));
    }

    @Test
    void canFindResourceInFilesystem() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("src/test/java/org/jbehave/io/resource.txt"), is("A test resource"));
    }

    @Test
    void canFindResourceFromRootDirectoryInFilesystem() throws IOException {
        ResourceFinder finder = new ResourceFinder("src/test/java/org/jbehave");
        assertThat(finder.resourceAsString("io/resource.txt"), is("A test resource"));
    }

    @Test
    void canFindResourceInJarsInClasspath() {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("ftl/jbehave-reports.ftl").length(), greaterThan(0));
    }

    @Test
    void cannotFindResourceFromInexistentClasspathDirectory() {
        ResourceFinder finder = new ResourceFinder("classpath:unexistent");
        assertThrows(ResourceNotFoundException.class, () -> finder.resourceAsString("resource.txt"));
    }

    @Test
    void cannotFindResourceFromInexistentFileDirectory() {
        ResourceFinder finder = new ResourceFinder("/unexistent");
        assertThrows(ResourceNotFoundException.class, () -> finder.resourceAsString("resource.txt"));
    }

}