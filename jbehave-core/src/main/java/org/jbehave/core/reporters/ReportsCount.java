package org.jbehave.core.reporters;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ReportsCount {

    private final int stories;
    private final int storiesNotAllowed;
    private final int storiesPending;
    private final int scenarios;
    private final int scenariosFailed;
    private final int scenariosNotAllowed;
    private final int scenariosPending;

    public ReportsCount(int stories, int storiesNotAllowed, int storiesPending, int scenarios, int scenariosFailed,
            int scenariosNotAllowed, int scenariosPending) {
        this.stories = stories;
        this.storiesNotAllowed = storiesNotAllowed;
        this.storiesPending = storiesPending;
        this.scenarios = scenarios;
        this.scenariosFailed = scenariosFailed;
        this.scenariosNotAllowed = scenariosNotAllowed;
        this.scenariosPending = scenariosPending;
    }

    public int getStories() {
        return stories;
    }

    public int getStoriesNotAllowed() {
        return storiesNotAllowed;
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

    public int getScenariosNotAllowed() {
        return scenariosNotAllowed;
    }

    public int getScenariosPending() {
        return scenariosPending;
    }

    public boolean failed(){
        if ( scenariosFailed > 0 ) return true;
        if ( stories > 0 && scenarios == 0 ) return true;
        return false;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
