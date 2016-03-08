package org.jbehave.core.model;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static java.util.Collections.unmodifiableList;

public class Story {

    private final String path;
    private final Description description;
    private final Narrative narrative;
    private final Meta meta;
    private final GivenStories givenStories;
    private final Lifecycle lifecycle;
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
        this(path, description, meta, narrative, GivenStories.EMPTY, scenarios);
    }

    public Story(String path, Description description, Meta meta, Narrative narrative, GivenStories givenStories, List<Scenario> scenarios) {
        this(path, description, meta, narrative, givenStories, Lifecycle.EMPTY, scenarios);
    }
    
    public Story(String path, Description description, Meta meta, Narrative narrative, GivenStories givenStories, Lifecycle lifecycle, List<Scenario> scenarios) {
        this.path = (path != null ? path : "");
        this.description = description;
        this.narrative = narrative;
        this.meta = meta;
        this.givenStories = givenStories;
        this.lifecycle = lifecycle;
        this.scenarios = scenarios;
    }

    public String getPath() {
        return path;
    }

    public Description getDescription() {
        return description;
    }

    public Narrative getNarrative() {
        return narrative;
    }

    public Meta asMeta(String prefix){
        Properties p = new Properties();
        p.setProperty(prefix+"path", path);
        p.setProperty(prefix+"description", description.asString());
        p.setProperty(prefix+"narrative", narrative.toString());
        return new Meta(p);
    }

    public Meta getMeta() {
        return meta;
    }

    public GivenStories getGivenStories(){
        return givenStories;
    }

    public Lifecycle getLifecycle(){
        return lifecycle;
    }
    
    public List<Scenario> getScenarios() {
        return unmodifiableList(scenarios);
    }

    public String getName() {
        return (name != null ? name : path);
    }

    public void namedAs(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
