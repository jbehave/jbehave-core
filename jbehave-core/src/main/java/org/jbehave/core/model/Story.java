package org.jbehave.core.model;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class Story {

    private final String id = UUID.randomUUID().toString();
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
        this(path, null, null , Arrays.<Scenario>asList());
    }

    public Story(String path, List<Scenario> scenarios) {
        this(path, null, null,null, scenarios);
    }

    public Story(List<Scenario> scenarios) {
        this(null, null, scenarios);
    }

    public Story(Description description, Narrative narrative, List<Scenario> scenarios) {
        this(null, description, narrative, scenarios);
    }

    public Story(String path, Description description, Narrative narrative, List<Scenario> scenarios) {
        this(path, description, null, narrative, scenarios);
    }

    public Story(String path, Description description, Meta meta, Narrative narrative, List<Scenario> scenarios) {
        this(path, description, meta, narrative, null, scenarios);
    }

    public Story(String path, Description description, Meta meta, Narrative narrative, GivenStories givenStories, List<Scenario> scenarios) {
        this(path, description, meta, narrative, givenStories, null, scenarios);
    }
    
    public Story(String path, Description description, Meta meta, Narrative narrative, GivenStories givenStories, Lifecycle lifecycle, List<Scenario> scenarios) {
        this.path = path;
        this.description = description;
        this.narrative = narrative;
        this.meta = meta;
        this.givenStories = givenStories;
        this.lifecycle = lifecycle;
        this.scenarios = scenarios;
    }

    public Story(Story story, String path, Lifecycle lifecycle) {
        this.path = path;
        this.description = story.description;
        this.narrative = story.narrative;
        this.meta = story.meta;
        this.givenStories = story.givenStories;
        this.lifecycle = lifecycle;
        this.scenarios = story.scenarios;
    }

    public String getPath() {
        if ( path == null ){
            return EMPTY;
        }
        return path;
    }

    public boolean hasDescription(){
        return description != null;
    }

    public Description getDescription() {
        if ( !hasDescription() ){
            return Description.EMPTY;
        }
        return description;
    }

    public boolean hasNarrative(){
        return narrative != null;
    }

    public Narrative getNarrative() {
        if ( !hasNarrative() ){
            return Narrative.EMPTY;
        }
        return narrative;
    }

    public boolean hasMeta() {
        return meta != null;
    }

    public Meta getMeta() {
        if ( !hasMeta() ){
            return Meta.EMPTY;
        }
        return meta;
    }

    public Meta asMeta(String prefix){
        Properties p = new Properties();
        p.setProperty(prefix+"path", getPath());
        p.setProperty(prefix+"description", getDescription().asString());
        p.setProperty(prefix+"narrative", getNarrative().toString());
        return new Meta(p);
    }

    public boolean hasGivenStories() {
        return givenStories != null;
    }

    public GivenStories getGivenStories(){
        if ( !hasGivenStories() ){
            return GivenStories.EMPTY;
        }
        return givenStories;
    }

    public boolean hasLifecycle() {
        return lifecycle != null;
    }

    public Lifecycle getLifecycle(){
        if ( !hasLifecycle() ){
            return Lifecycle.EMPTY;
        }
        return lifecycle;
    }
    
    public List<Scenario> getScenarios() {
        return unmodifiableList(scenarios);
    }

    public String getName() {
        return (name != null ? name : getPath());
    }

    public String getId() {
        return id;
    }

    public void namedAs(String name) {
        this.name = name;
    }

    public Story cloneWithScenarios(List<Scenario> scenarios) {
        Story story = new Story(path, description, meta, narrative, givenStories, lifecycle, scenarios);
        story.namedAs(name);
        return story;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
