package org.jbehave.core.model;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

public class Scenario {

    private final String title;
    private final List<String> givenStoryPaths;
    private final List<String> steps;
    private final ExamplesTable table;

    public Scenario() {
        this("");
    }

    public Scenario(String title) {
        this(title, new ArrayList<String>());
    }

    public Scenario(List<String> steps) {
        this("", steps);
    }

    public Scenario(String title, List<String> steps) {
        this(title, new ArrayList<String>(), new ExamplesTable(""), steps);
    }

    public Scenario(String title, List<String> givenStoryPaths, List<String> steps) {
        this(title, givenStoryPaths, new ExamplesTable(""), steps);
    }

    public Scenario(String title, ExamplesTable table, String... steps) {
        this(title, new ArrayList<String>(), table, asList(steps));
    }
    
    public Scenario(String title, List<String> givenStoryPaths, ExamplesTable table, List<String> steps) {
        this.title = title;
        this.givenStoryPaths = givenStoryPaths;
        this.steps = steps;
        this.table = table;
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

    public ExamplesTable getTable() {
        return table;
    }

}
