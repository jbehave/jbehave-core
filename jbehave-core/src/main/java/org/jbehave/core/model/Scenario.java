package org.jbehave.core.model;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Scenario {

    private final String title;
    private final Meta meta;
    private final List<String> givenStoryPaths;
    private final List<String> steps;
    private final ExamplesTable examplesTable;

    public Scenario() {
        this(Arrays.<String>asList());
    }

    public Scenario(List<String> steps) {
        this("", steps);
    }

    public Scenario(String title, Meta meta) {
        this(title, meta, Arrays.<String>asList(), new ExamplesTable(""), Arrays.<String>asList());
    }

    public Scenario(String title, List<String> steps) {
        this(title, Arrays.<String>asList(), new ExamplesTable(""), steps);
    }

    public Scenario(String title, List<String> givenStoryPaths, List<String> steps) {
        this(title, givenStoryPaths, new ExamplesTable(""), steps);
    }
    
    public Scenario(String title, List<String> givenStoryPaths, ExamplesTable examplesTable, List<String> steps) {
        this(title, Meta.EMPTY, givenStoryPaths, examplesTable, steps);
    }

    public Scenario(String title, Meta meta, List<String> givenStoryPaths, ExamplesTable examplesTable, List<String> steps) {
        this.title = title;
        this.meta = meta;
        this.givenStoryPaths = givenStoryPaths;
        this.steps = steps;
        this.examplesTable = examplesTable;
    }

    public Meta getMeta(){
        return meta;
    }
    
    public List<String> getGivenStoryPaths() {
        return unmodifiableList(givenStoryPaths);
    }

    public List<String> getSteps() {
        return unmodifiableList(steps);
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
