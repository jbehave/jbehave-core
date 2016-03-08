package org.jbehave.core.model;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Scenario {

    private final String title;
    private final Meta meta;
    private final GivenStories givenStories;
    private final ExamplesTable examplesTable;
    private final List<String> steps;

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
        this.examplesTable = examplesTable;
        this.steps = steps;
    }
    
    public String getTitle() {
        return title;
    }

    public GivenStories getGivenStories() {
        return givenStories;
    }

    public ExamplesTable getExamplesTable() {
        return examplesTable;
    }

    public Meta asMeta(String prefix){
        Properties p = new Properties();
        p.setProperty(prefix+"title", title);
        p.setProperty(prefix+"givenStories", givenStories.asString());
        p.setProperty(prefix+"examplesTable", examplesTable.asString());
        return new Meta(p);
    }

    public Meta getMeta(){
        return meta;
    }

    public List<String> getSteps() {
        return unmodifiableList(steps);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
