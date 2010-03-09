package org.jbehave.scenario.definition;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

public class StoryDefinition {

    private final Blurb blurb;
    private final Narrative narrative;
    private final List<ScenarioDefinition> scenarioDefinitions;
    private String name = "Story";
    private String path = "";

    public StoryDefinition(ScenarioDefinition... scenarioDefinitions) {
        this(asList(scenarioDefinitions));
    }

    public StoryDefinition(List<ScenarioDefinition> scenarioDefinitions) {
        this(Blurb.EMPTY, Narrative.EMPTY, scenarioDefinitions);
    }

    public StoryDefinition(Blurb blurb, ScenarioDefinition... scenarioDefinitions) {
        this(blurb, Narrative.EMPTY, asList(scenarioDefinitions));
    }

    public StoryDefinition(Blurb blurb, Narrative narrative, List<ScenarioDefinition> scenarioDefinitions) {
        this(blurb, narrative, "", scenarioDefinitions);
    }

    public StoryDefinition(Blurb blurb, Narrative narrative, String path, List<ScenarioDefinition> scenarioDefinitions) {
        this.blurb = blurb;
        this.narrative = narrative;
        this.path = path;
        this.scenarioDefinitions = scenarioDefinitions;
    }

    public Blurb getBlurb() {
        return blurb;
    }

    public Narrative getNarrative() {
        return narrative;
    }

    public List<ScenarioDefinition> getScenarios() {
        return unmodifiableList(scenarioDefinitions);
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
