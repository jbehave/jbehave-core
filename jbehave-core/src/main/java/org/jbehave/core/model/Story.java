package org.jbehave.core.model;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

public class Story {

    private final Description description;
    private final Narrative narrative;
    private final List<Scenario> scenarios;
    private String name = "Story";
    private String path = "";

    public Story(Scenario... scenarios) {
        this(asList(scenarios));
    }

    public Story(List<Scenario> scenarios) {
        this(Description.EMPTY, Narrative.EMPTY, scenarios);
    }

    public Story(Description description, Scenario... scenarios) {
        this(description, Narrative.EMPTY, asList(scenarios));
    }

    public Story(Description description, Narrative narrative, List<Scenario> scenarios) {
        this(description, narrative, "", scenarios);
    }

    public Story(Description description, Narrative narrative, String path, List<Scenario> scenarios) {
        this.description = description;
        this.narrative = narrative;
        this.path = path;
        this.scenarios = scenarios;
    }

    public Description getDescription() {
        return description;
    }

    public Narrative getNarrative() {
        return narrative;
    }

    public List<Scenario> getScenarios() {
        return unmodifiableList(scenarios);
    }

    public String getName() {
        return name;
    }

    public void namedAs(String name) {
        this.name = name;
    }

    public String getPath() {
        return (path != null ? path : "");
    }
}
