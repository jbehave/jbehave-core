package org.jbehave.core.reporters;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ReportsCount {

    private final int stories;
    private final int storiesExcluded;
    private final int storiesPending;
    private final int scenarios;
    private final int scenariosFailed;
    private final int scenariosExcluded;
    private final int scenariosPending;
    private final int stepsFailed;

    public ReportsCount(int stories, int storiesExcluded, int storiesPending, int scenarios, int scenariosFailed,
                        int scenariosExcluded, int scenariosPending, int stepsFailed) {
        this.stories = stories;
        this.storiesExcluded = storiesExcluded;
        this.storiesPending = storiesPending;
        this.scenarios = scenarios;
        this.scenariosFailed = scenariosFailed;
        this.scenariosExcluded = scenariosExcluded;
        this.scenariosPending = scenariosPending;
        this.stepsFailed = stepsFailed;
    }

    public int getStories() {
        return stories;
    }

    public int getStoriesExcluded() {
        return storiesExcluded;
    }

    public int getStoriesPending() {
        return storiesPending;
    }

    public int getScenarios() {
        return scenarios;
    }

    public int getScenariosFailed() {
        return scenariosFailed;
    }

    public int getScenariosExcluded() {
        return scenariosExcluded;
    }

    public int getScenariosPending() {
        return scenariosPending;
    }
    
    public int getStepFailed(){
        return stepsFailed;
    }

    public boolean failed(){
        return scenariosFailed > 0 || stepsFailed > 0;
    }

    public boolean pending(){
        return scenariosPending > 0 || storiesPending > 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
