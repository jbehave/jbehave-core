package org.jbehave.core.model;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static java.util.Collections.unmodifiableList;

public class Story {

    private final String path;
    private final Description description;
    private final Meta meta;
    private final Narrative narrative;
    private final List<Scenario> scenarios;
    private String name;

    public Story() {
        this(Arrays.<Scenario>asList());
    }

    public Story(String path) {
        this(path, Description.EMPTY, Narrative.EMPTY, Arrays.<Scenario>asList());
    }

    public Story(List<Scenario> scenarios) {
        this(Description.EMPTY, Narrative.EMPTY, scenarios);
    }

    public Story(Description description, Narrative narrative, List<Scenario> scenarios) {
        this((String)null, description, narrative, scenarios);
    }

    public Story(String path, Description description, Narrative narrative, List<Scenario> scenarios) {
        this(path, description, Meta.EMPTY, narrative, scenarios);
    }

    public Story(String path, Description description, Meta meta, Narrative narrative, List<Scenario> scenarios) {
        this.path = path;
        this.description = description;
        this.meta = meta;
        this.narrative = narrative;
        this.scenarios = scenarios;
    }

    public Description getDescription() {
        return description;
    }

    public Meta getMeta() {
        return meta;
    }

    public Narrative getNarrative() {
        return narrative;
    }

    public List<Scenario> getScenarios() {
        return unmodifiableList(scenarios);
    }

    public String getName() {
        return (name != null ? name : getPath());
    }

    public void namedAs(String name) {
        this.name = name;
    }

    public String getPath() {
        return (path != null ? path : "");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
