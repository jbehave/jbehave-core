package org.jbehave.core.configuration.groovy;

import static java.util.Arrays.asList;

import java.net.URL;
import java.util.List;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;

public class GroovyResourceFinder {

    private URL codeLocation;
    private String include;
    private String exclude;

    public GroovyResourceFinder(){
        this(CodeLocations.codeLocationFromPath("src/main/groovy"), "**/*.groovy", "");
    }

    public GroovyResourceFinder(URL codeLocation, String include, String exclude) {
        this.codeLocation = codeLocation;
        this.include = include;
        this.exclude = exclude;
    }

    public List<String> findResources() {
        return new StoryFinder().findPaths(codeLocation.getFile(), asList(include), asList(exclude));
    }

}