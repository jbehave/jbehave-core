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
    private final int stepsFailed;

    public ReportsCount(int stories, int storiesNotAllowed, int storiesPending, int scenarios, int scenariosFailed,
            int scenariosNotAllowed, int scenariosPending, int stepsFailed) {
        this.stories = stories;
        this.storiesNotAllowed = storiesNotAllowed;
        this.storiesPending = storiesPending;
        this.scenarios = scenarios;
        this.scenariosFailed = scenariosFailed;
        this.scenariosNotAllowed = scenariosNotAllowed;
        this.scenariosPending = scenariosPending;
        this.stepsFailed = stepsFailed;
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
    
    public int getStepFailed(){
        return stepsFailed;
    }

    public boolean failed(){
        if ( scenariosFailed > 0 || stepsFailed > 0 ) return true;
// JBEHAVE-472:  find a better way to express failures before scenarios        
//        if ( stories > 0 && scenarios == 0 ) return true;
        return false;
    }

    public boolean pending(){
        if ( scenariosPending > 0 || storiesPending > 0 ) return true;
        return false;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
