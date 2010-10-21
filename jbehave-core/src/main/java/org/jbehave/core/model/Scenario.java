package org.jbehave.core.model;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Scenario {

    private final String title;
    private final Meta meta;
    private final List<String> steps;
    private final ExamplesTable examplesTable;
    private final GivenStories givenStories;

    public Scenario() {
        this(Arrays.<String>asList());
    }

    public Scenario(List<String> steps) {
        this("", steps);
    }

    public Scenario(String title, Meta meta) {
        this(title, meta, GivenStories.EMPTY, ExamplesTable.EMPTY, Arrays.<String>asList());
    }

    public Scenario(String title, List<String> steps) {
        this(title, Meta.EMPTY, GivenStories.EMPTY, ExamplesTable.EMPTY, steps);
    }

    public Scenario(String title, Meta meta, GivenStories givenStories, ExamplesTable examplesTable, List<String> steps) {
        this.title = title;
        this.meta = meta;
        this.givenStories = givenStories;
        this.steps = steps;
        this.examplesTable = examplesTable;
    }
    
    public Meta getMeta(){
        return meta;
    }

    public GivenStories getGivenStories() {
        return givenStories;
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
