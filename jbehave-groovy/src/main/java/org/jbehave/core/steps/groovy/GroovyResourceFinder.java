package org.jbehave.core.steps.groovy;

import static java.util.Arrays.asList;

import java.net.URL;
import java.util.List;

import org.jbehave.core.io.StoryFinder;

public class GroovyResourceFinder {

    private String include;
    private String exclude;
    private URL codeLocation;

    public GroovyResourceFinder(URL codeLocation, String include, String exclude) {
        this.include = include;
        this.exclude = exclude;
        this.codeLocation = codeLocation;
    }

    public List<String> findResources() {
        return new StoryFinder().findPaths(codeLocation.getFile(), asList(include), asList(exclude));
    }

}