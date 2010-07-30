package org.jbehave.core.model;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Scenario {

    private final String title;
    private final List<String> givenStoryPaths;
    private final List<String> steps;
    private final ExamplesTable examplesTable;

    public Scenario() {
        this(Arrays.<String>asList());
    }

    public Scenario(List<String> steps) {
        this("", steps);
    }

    public Scenario(String title, List<String> steps) {
        this(title, Arrays.<String>asList(), new ExamplesTable(""), steps);
    }

    public Scenario(String title, List<String> givenStoryPaths, List<String> steps) {
        this(title, givenStoryPaths, new ExamplesTable(""), steps);
    }
    
    public Scenario(String title, List<String> givenStoryPaths, ExamplesTable examplesTable, List<String> steps) {
        this.title = title;
        this.givenStoryPaths = givenStoryPaths;
        this.steps = steps;
        this.examplesTable = examplesTable;
    }

    public List<String> getGivenStoryPaths() {
        return givenStoryPaths;
    }

    public List<String> getSteps() {
        return steps;
    }

    public String getTitle() {
        return title;
    }

    public ExamplesTable getExamplesTable() {
        return examplesTable;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
