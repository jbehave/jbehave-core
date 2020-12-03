package org.jbehave.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jbehave.io.ResourceFinder.ResourceNotFoundException;
import org.junit.Test;

public class ResourceFinderBehaviour {

    @Test
    public void canFindResourceInClasspath() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        assertEquals("A test resource", finder.resourceAsString("org/jbehave/io/resource.txt"));
    }

    @Test
    public void canFindResourceFromRootDirectoryInClasspath() throws IOException {
        ResourceFinder finder = new ResourceFinder("classpath:org/jbehave");
        assertEquals("A test resource", finder.resourceAsString("io/resource.txt"));
    }

    @Test
    public void canChangeRootDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        finder.useRootDirectory("classpath:org/jbehave/io");
        assertEquals("A test resource", finder.resourceAsString("resource.txt"));
    }

    @Test
    public void canFindResourceInFilesystem() throws IOException {
        ResourceFinder finder = new ResourceFinder();
        assertEquals("A test resource", finder.resourceAsString("src/test/java/org/jbehave/io/resource.txt"));
    }

    @Test
    public void canFindResourceFromRootDirectoryInFilesystem() throws IOException {
        ResourceFinder finder = new ResourceFinder("src/test/java/org/jbehave");
        assertEquals("A test resource", finder.resourceAsString("io/resource.txt"));
    }

    @Test
    public void canFindResourceInJarsInClasspath() {
        ResourceFinder finder = new ResourceFinder();
        assertThat(finder.resourceAsString("ftl/jbehave-reports.ftl").length(), greaterThan(0));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void cannotFindResourceFromInexistentClasspathDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder("classpath:inexistent");
        finder.resourceAsString("resource.txt");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void cannotFindResourceFromInexistentFileDirectory() throws IOException {
        ResourceFinder finder = new ResourceFinder("/inexistent");
        finder.resourceAsString("resource.txt");
    }

}